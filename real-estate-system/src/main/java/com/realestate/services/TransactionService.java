package com.realestate.services;

import com.realestate.models.Property;
import com.realestate.models.User;
import com.realestate.exceptions.PropertyException;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * Service class for managing property transactions.
 * Demonstrates concurrent processing and transaction management.
 */
public class TransactionService {
    // Singleton instance
    private static TransactionService instance;
    
    // Services
    private final PropertyService propertyService;
    private final UserService userService;
    
    // Thread pool for processing transactions
    private final ExecutorService transactionExecutor;
    
    // Transaction storage
    private final Map<String, Transaction> transactionHistory;
    private final Map<String, Set<String>> userTransactions;
    private final Map<String, Set<String>> propertyTransactions;
    
    // Transaction status tracking
    private final Map<String, TransactionStatus> transactionStatus;
    private final BlockingQueue<Transaction> pendingTransactions;
    
    // Constants
    private static final double COMMISSION_RATE = 0.03; // 3%
    private static final long TRANSACTION_TIMEOUT = 30; // minutes

    /**
     * Inner class to represent a transaction
     */
    public class Transaction {
        private final String transactionId;
        private final String propertyId;
        private final String buyerId;
        private final String sellerId;
        private final double amount;
        private final LocalDateTime timestamp;
        private TransactionStatus status;
        private String notes;

        public Transaction(String propertyId, String buyerId, String sellerId, double amount) {
            this.transactionId = UUID.randomUUID().toString();
            this.propertyId = propertyId;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
            this.status = TransactionStatus.PENDING;
        }

        // Getters
        public String getTransactionId() { return transactionId; }
        public String getPropertyId() { return propertyId; }
        public String getBuyerId() { return buyerId; }
        public String getSellerId() { return sellerId; }
        public double getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public TransactionStatus getStatus() { return status; }
        public String getNotes() { return notes; }

        // Setters
        public void setStatus(TransactionStatus status) { 
            this.status = status; 
        }
        public void setNotes(String notes) { 
            this.notes = notes; 
        }
    }

    /**
     * Enum for transaction status
     */
    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // Private constructor for singleton pattern
    private TransactionService() {
        this.propertyService = PropertyService.getInstance();
        this.userService = UserService.getInstance();
        this.transactionExecutor = Executors.newFixedThreadPool(5);
        this.transactionHistory = new ConcurrentHashMap<>();
        this.userTransactions = new ConcurrentHashMap<>();
        this.propertyTransactions = new ConcurrentHashMap<>();
        this.transactionStatus = new ConcurrentHashMap<>();
        this.pendingTransactions = new LinkedBlockingQueue<>();
        
        // Start transaction processor
        startTransactionProcessor();
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized TransactionService getInstance() {
        if (instance == null) {
            instance = new TransactionService();
        }
        return instance;
    }

    /**
     * Initiates a property transaction
     */
    public String initiateTransaction(String propertyId, String buyerId, String sellerId, 
                                    double amount, String token) {
        // Validate user authorization
        if (!userService.validateToken(token)) {
            throw new PropertyException("Unauthorized transaction", 
                PropertyException.UNAUTHORIZED);
        }

        // Validate property and users
        Property property = propertyService.getProperty(propertyId);
        User buyer = userService.getUser(buyerId);
        User seller = userService.getUser(sellerId);

        // Create transaction
        Transaction transaction = new Transaction(propertyId, buyerId, sellerId, amount);
        
        // Store transaction
        transactionHistory.put(transaction.getTransactionId(), transaction);
        transactionStatus.put(transaction.getTransactionId(), TransactionStatus.PENDING);
        
        // Update indexes
        updateTransactionIndexes(transaction);
        
        // Add to processing queue
        pendingTransactions.offer(transaction);
        
        return transaction.getTransactionId();
    }

    /**
     * Gets transaction status
     */
    public TransactionStatus getTransactionStatus(String transactionId) {
        return transactionStatus.getOrDefault(transactionId, TransactionStatus.FAILED);
    }

    /**
     * Gets transaction details
     */
    public Transaction getTransaction(String transactionId) {
        Transaction transaction = transactionHistory.get(transactionId);
        if (transaction == null) {
            throw new PropertyException("Transaction not found", 
                PropertyException.PROPERTY_NOT_FOUND);
        }
        return transaction;
    }

    /**
     * Gets user's transactions
     */
    public List<Transaction> getUserTransactions(String userId) {
        Set<String> transactionIds = userTransactions.getOrDefault(userId, Collections.emptySet());
        List<Transaction> transactions = new ArrayList<>();
        for (String id : transactionIds) {
            Transaction transaction = transactionHistory.get(id);
            if (transaction != null) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    /**
     * Gets property's transactions
     */
    public List<Transaction> getPropertyTransactions(String propertyId) {
        Set<String> transactionIds = propertyTransactions.getOrDefault(propertyId, Collections.emptySet());
        List<Transaction> transactions = new ArrayList<>();
        for (String id : transactionIds) {
            Transaction transaction = transactionHistory.get(id);
            if (transaction != null) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    /**
     * Cancels a pending transaction
     */
    public void cancelTransaction(String transactionId, String token) {
        if (!userService.validateToken(token)) {
            throw new PropertyException("Unauthorized cancellation", 
                PropertyException.UNAUTHORIZED);
        }

        Transaction transaction = transactionHistory.get(transactionId);
        if (transaction == null) {
            throw new PropertyException("Transaction not found", 
                PropertyException.PROPERTY_NOT_FOUND);
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new PropertyException("Cannot cancel non-pending transaction", 
                PropertyException.VALIDATION_ERROR);
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionStatus.put(transactionId, TransactionStatus.CANCELLED);
    }

    // Private helper methods

    private void startTransactionProcessor() {
        Thread processor = new Thread(() -> {
            while (true) {
                try {
                    Transaction transaction = pendingTransactions.take();
                    processTransaction(transaction);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        processor.setDaemon(true);
        processor.start();
    }

    private void processTransaction(Transaction transaction) {
        transactionExecutor.submit(() -> {
            try {
                // Update status to processing
                transaction.setStatus(TransactionStatus.PROCESSING);
                transactionStatus.put(transaction.getTransactionId(), TransactionStatus.PROCESSING);

                // Perform transaction steps
                validateTransaction(transaction);
                updatePropertyStatus(transaction);
                calculateAndDistributeFees(transaction);

                // Mark as completed
                transaction.setStatus(TransactionStatus.COMPLETED);
                transactionStatus.put(transaction.getTransactionId(), TransactionStatus.COMPLETED);

            } catch (Exception e) {
                // Handle failure
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setNotes("Failed: " + e.getMessage());
                transactionStatus.put(transaction.getTransactionId(), TransactionStatus.FAILED);
            }
        });
    }

    private void validateTransaction(Transaction transaction) {
        Property property = propertyService.getProperty(transaction.getPropertyId());
        if (!property.isAvailable()) {
            throw new PropertyException("Property is not available", 
                PropertyException.VALIDATION_ERROR);
        }
    }

    private void updatePropertyStatus(Transaction transaction) {
        Property property = propertyService.getProperty(transaction.getPropertyId());
        property.setAvailable(false);
        propertyService.updateProperty(transaction.getPropertyId(), property);
    }

    private void calculateAndDistributeFees(Transaction transaction) {
        double commission = transaction.getAmount() * COMMISSION_RATE;
        // In a real system, this would handle the actual money transfer
    }

    private void updateTransactionIndexes(Transaction transaction) {
        // Update user transactions
        userTransactions.computeIfAbsent(transaction.getBuyerId(), k -> ConcurrentHashMap.newKeySet())
                       .add(transaction.getTransactionId());
        userTransactions.computeIfAbsent(transaction.getSellerId(), k -> ConcurrentHashMap.newKeySet())
                       .add(transaction.getTransactionId());
        
        // Update property transactions
        propertyTransactions.computeIfAbsent(transaction.getPropertyId(), k -> ConcurrentHashMap.newKeySet())
                          .add(transaction.getTransactionId());
    }
}
