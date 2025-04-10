package com.realestate;

import com.realestate.models.*;
import com.realestate.services.*;
import com.realestate.exceptions.PropertyException;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SystemDemo {
    private static PropertyService propertyService;
    private static UserService userService;
    private static TransactionService transactionService;
    private static SearchService searchService;
    private static AnalyticsService analyticsService;
    private static NotificationService notificationService;
    
    // Store IDs for reference
    private static String adminId;
    private static String customerId;
    private static String adminToken;
    private static String customerToken;
    private static String residentialId;
    private static String commercialId;

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static void main(String[] args) {
        initializeServices();
        
        try {
            // Demonstrate user management
            demonstrateUserManagement();
            
            // Demonstrate property management
            demonstratePropertyManagement();
            
            // Demonstrate search functionality
            demonstrateSearch();
            
            // Demonstrate transaction processing
            demonstrateTransactions();
            
            // Demonstrate analytics
            demonstrateAnalytics();
            
            // Demonstrate notifications
            demonstrateNotifications();
            
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeServices() {
        System.out.println("Initializing services...");
        propertyService = PropertyService.getInstance();
        userService = UserService.getInstance();
        transactionService = TransactionService.getInstance();
        searchService = SearchService.getInstance();
        analyticsService = AnalyticsService.getInstance();
        notificationService = NotificationService.getInstance();
        System.out.println("Services initialized successfully.\n");
    }

    private static void demonstrateUserManagement() {
        System.out.println("=== User Management Demonstration ===");
        
        try {
            // Hash passwords before creating users
            String adminPass = hashPassword("password123");
            String customerPass = hashPassword("password456");
            
            // Create and register admin user
            AdminUser admin = new AdminUser(
                "John", "Admin", "john.admin@example.com",
                "1234567890", adminPass, "Sales",
                "Manager", true
            );
            adminId = userService.registerUser(admin, "password123");
            System.out.println("Admin registered with ID: " + adminId);
            
            // Create and register customer user
            CustomerUser customer = new CustomerUser(
                "Alice", "Customer", "alice@example.com",
                "9876543210", customerPass, "123 Main St",
                new Date(), "email", 500000.0
            );
            customerId = userService.registerUser(customer, "password456");
            System.out.println("Customer registered with ID: " + customerId);
            
            // Login demonstration
            adminToken = userService.login("john.admin@example.com", "password123");
            System.out.println("Admin logged in successfully with token: " + adminToken);
            
            customerToken = userService.login("alice@example.com", "password456");
            System.out.println("Customer logged in successfully with token: " + customerToken);
            
        } catch (PropertyException e) {
            System.err.println("User management error: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static void demonstratePropertyManagement() {
        System.out.println("=== Property Management Demonstration ===");
        
        try {
            // Create residential property
            Property residential = PropertyFactory.createResidentialProperty(
                "Luxury Villa", "Beautiful 3-bedroom villa",
                750000.0, 2500.0, "456 Park Ave",
                "New York", "NY", "10001",
                3, 2, true, true, "Villa"
            );
            
            // Create commercial property
            Property commercial = PropertyFactory.createCommercialProperty(
                "Office Space", "Modern office in business district",
                1500000.0, 5000.0, "789 Business Blvd",
                "New York", "NY", "10002",
                "Office", 10, true, 20, "Commercial"
            );
            
            // Add properties using admin token
            residentialId = propertyService.addProperty(residential);
            commercialId = propertyService.addProperty(commercial);
            
            System.out.println("Residential property added with ID: " + residentialId);
            System.out.println("Commercial property added with ID: " + commercialId);
            
            // Update property
            residential.setPrice(800000.0);
            propertyService.updateProperty(residentialId, residential);
            System.out.println("Property price updated successfully");
            
            // Get and display property details
            Property retrievedProperty = propertyService.getProperty(residentialId);
            System.out.println("Retrieved property: " + retrievedProperty.getTitle() + 
                             " - $" + retrievedProperty.getPrice());
            
        } catch (PropertyException e) {
            System.err.println("Property management error: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static void demonstrateSearch() {
        System.out.println("=== Search Functionality Demonstration ===");
        
        // Create search criteria
        SearchService.SearchCriteria criteria = new SearchService.SearchCriteria()
            .withPriceRange(500000.0, 1000000.0)
            .withLocation("New York", "NY")
            .withPropertyType("Residential")
            .withResidentialCriteria(3, 2)
            .withSorting(SearchService.SortOption.PRICE_ASC);
        
        // Perform search
        SearchService.SearchResult result = searchService.searchProperties(criteria);
        
        System.out.println("Search results:");
        System.out.println("Total matches: " + result.getTotalResults());
        
        // Display found properties
        for (Property property : result.getProperties()) {
            System.out.printf("Found: %s - $%.2f%n", 
                property.getTitle(), property.getPrice());
        }
        
        System.out.println();
    }

    private static void demonstrateTransactions() {
        System.out.println("=== Transaction Processing Demonstration ===");
        
        try {
            // Initiate transaction using the actual property and user IDs
            String transactionId = transactionService.initiateTransaction(
                residentialId, customerId, adminId,
                800000.0, customerToken  // Using customer token to initiate purchase
            );
            
            System.out.println("Transaction initiated with ID: " + transactionId);
            
            // Process the transaction
            TransactionService.Transaction transaction = 
                transactionService.getTransaction(transactionId);
            
            // Check initial status
            System.out.println("Initial transaction status: " + 
                transactionService.getTransactionStatus(transactionId));
            
            // Wait a bit for processing
            Thread.sleep(1000);
            
            // Check final status
            TransactionService.TransactionStatus status = 
                transactionService.getTransactionStatus(transactionId);
            System.out.println("Final transaction status: " + status);
            
        } catch (PropertyException | InterruptedException e) {
            System.err.println("Transaction error: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static void demonstrateAnalytics() {
        System.out.println("=== Analytics Demonstration ===");
        
        // Get market trends
        AnalyticsService.MarketTrends trends = 
            analyticsService.getMarketTrends("New York", "NY");
        
        System.out.println("Market Analysis:");
        System.out.printf("Average Price: $%.2f%n", trends.getAveragePrice());
        System.out.printf("Price Change: %.1f%%%n", trends.getPriceChangePercent());
        System.out.println("Total Listings: " + trends.getTotalListings());
        
        // Get property metrics using actual property ID
        try {
            AnalyticsService.PropertyMetrics metrics = 
                analyticsService.getPropertyMetrics(residentialId);
            
            System.out.println("\nProperty Metrics for " + residentialId + ":");
            System.out.println("Views: " + metrics.getViewCount());
            System.out.println("Days on Market: " + metrics.getDaysOnMarket());
        } catch (Exception e) {
            System.err.println("Error getting property metrics: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static void demonstrateNotifications() {
        System.out.println("=== Notification System Demonstration ===");
        
        // Create notification observer
        NotificationService.NotificationObserver observer = notification -> 
            System.out.println("Received notification: " + notification.getTitle() +
                             "\nMessage: " + notification.getMessage());
        
        // Register observer for the customer
        notificationService.registerObserver(customerId, observer);
        
        // Send notification about the property
        Map<String, String> metadata = new HashMap<>();
        metadata.put("propertyId", residentialId);
        
        notificationService.sendNotification(
            NotificationService.NotificationType.PRICE_CHANGE,
            customerId,
            "Price Update",
            "Property price has been updated to $800,000",
            metadata
        );
        
        // Send transaction notification
        notificationService.sendNotification(
            NotificationService.NotificationType.TRANSACTION_UPDATE,
            customerId,
            "Transaction Processing",
            "Your purchase request for Luxury Villa is being processed",
            metadata
        );
        
        // Get user notifications
        List<NotificationService.Notification> notifications = 
            notificationService.getUserNotifications(customerId);
        
        System.out.println("\nNotification Summary:");
        System.out.println("Total notifications: " + notifications.size());
        System.out.println("Unread notifications: " + 
            notificationService.getUnreadCount(customerId));
        
        System.out.println();
    }
}
