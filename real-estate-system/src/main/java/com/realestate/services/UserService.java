package com.realestate.services;

import com.realestate.models.User;
import com.realestate.models.AdminUser;
import com.realestate.models.CustomerUser;
import com.realestate.exceptions.PropertyException;
import com.realestate.utils.PropertyUtils;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

/**
 * Service class for managing users in the system.
 * Demonstrates user authentication, authorization, and session management.
 */
public class UserService {
    // Singleton instance
    private static UserService instance;
    
    // Thread-safe collections for user management
    private final Map<String, User> userDatabase;
    private final Map<String, String> emailToIdMap;
    private final Map<String, String> activeSessionTokens;
    private final Set<String> blacklistedTokens;
    
    // Constants
    private static final int SESSION_TIMEOUT_HOURS = 24;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private final Map<String, Integer> loginAttempts;
    
    // Private constructor for singleton pattern
    private UserService() {
        this.userDatabase = new ConcurrentHashMap<>();
        this.emailToIdMap = new ConcurrentHashMap<>();
        this.activeSessionTokens = new ConcurrentHashMap<>();
        this.blacklistedTokens = ConcurrentHashMap.newKeySet();
        this.loginAttempts = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Registers a new user
     */
    public String registerUser(User user, String password) {
        // Validate user data
        if (!user.validateUser()) {
            throw new PropertyException("Invalid user data", 
                PropertyException.VALIDATION_ERROR);
        }

        // Check if email is already registered
        if (emailToIdMap.containsKey(user.getEmail().toLowerCase())) {
            throw new PropertyException("Email already registered", 
                PropertyException.DUPLICATE_PROPERTY);
        }

        // Hash password
        String hashedPassword = hashPassword(password);
        
        // Store user
        synchronized (this) {
            userDatabase.put(user.getUserId(), user);
            emailToIdMap.put(user.getEmail().toLowerCase(), user.getUserId());
        }

        return user.getUserId();
    }

    /**
     * Authenticates a user and returns a session token
     */
    public String login(String email, String password) {
        // Check login attempts
        if (isAccountLocked(email)) {
            throw new PropertyException("Account is temporarily locked", 
                PropertyException.UNAUTHORIZED);
        }

        // Get user
        String userId = emailToIdMap.get(email.toLowerCase());
        if (userId == null) {
            incrementLoginAttempts(email);
            throw new PropertyException("Invalid credentials", 
                PropertyException.UNAUTHORIZED);
        }

        User user = userDatabase.get(userId);
        
        // Verify password
        if (!verifyPassword(password, user)) {
            incrementLoginAttempts(email);
            throw new PropertyException("Invalid credentials", 
                PropertyException.UNAUTHORIZED);
        }

        // Reset login attempts on successful login
        loginAttempts.remove(email);

        // Generate and store session token
        String token = generateSessionToken(user);
        activeSessionTokens.put(token, userId);
        
        // Update last login
        user.updateLastLoginDate();
        
        return token;
    }

    /**
     * Logs out a user
     */
    public void logout(String token) {
        activeSessionTokens.remove(token);
        blacklistedTokens.add(token);
    }

    /**
     * Gets user by ID
     */
    public User getUser(String userId) {
        User user = userDatabase.get(userId);
        if (user == null) {
            throw new PropertyException("User not found", 
                PropertyException.PROPERTY_NOT_FOUND);
        }
        return user;
    }

    /**
     * Updates user information
     */
    public void updateUser(String userId, User updatedUser) {
        if (!userDatabase.containsKey(userId)) {
            throw new PropertyException("User not found", 
                PropertyException.PROPERTY_NOT_FOUND);
        }

        if (!updatedUser.validateUser()) {
            throw new PropertyException("Invalid user data", 
                PropertyException.VALIDATION_ERROR);
        }

        userDatabase.put(userId, updatedUser);
    }

    /**
     * Validates session token
     */
    public boolean validateToken(String token) {
        if (token == null || blacklistedTokens.contains(token)) {
            return false;
        }

        String userId = activeSessionTokens.get(token);
        if (userId == null) {
            return false;
        }

        // Check if token is expired
        // In a real system, token would contain timestamp information
        return true;
    }

    /**
     * Checks if user has specific permission
     */
    public boolean hasPermission(String token, String operation) {
        if (!validateToken(token)) {
            return false;
        }

        String userId = activeSessionTokens.get(token);
        User user = userDatabase.get(userId);
        
        return user != null && user.hasPermission(operation);
    }

    // Private helper methods

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, User user) {
        String hashedPassword = hashPassword(password);
        return user.verifyPassword(hashedPassword);
    }

    private String generateSessionToken(User user) {
        String tokenBase = user.getUserId() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenBase.getBytes());
    }

    private void incrementLoginAttempts(String email) {
        loginAttempts.compute(email, (k, v) -> (v == null) ? 1 : v + 1);
    }

    private boolean isAccountLocked(String email) {
        Integer attempts = loginAttempts.get(email);
        return attempts != null && attempts >= MAX_LOGIN_ATTEMPTS;
    }

    /**
     * Creates an admin user
     */
    public AdminUser createAdminUser(String firstName, String lastName, String email,
                                   String phoneNumber, String password, String department,
                                   String role, boolean isSuperAdmin) {
        AdminUser admin = new AdminUser(firstName, lastName, email, phoneNumber,
                                      password, department, role, isSuperAdmin);
        registerUser(admin, password);
        return admin;
    }

    /**
     * Creates a customer user
     */
    public CustomerUser createCustomerUser(String firstName, String lastName, String email,
                                         String phoneNumber, String password, String address,
                                         Date registrationDate, String preferredContact,
                                         double budget) {
        CustomerUser customer = new CustomerUser(firstName, lastName, email, phoneNumber,
                                               password, address, registrationDate,
                                               preferredContact, budget);
        registerUser(customer, password);
        return customer;
    }
}
