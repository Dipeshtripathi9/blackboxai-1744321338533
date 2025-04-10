package com.realestate.models;

import java.util.Date;
import java.util.UUID;

/**
 * Abstract base class for all user types in the system.
 * Implements core user attributes and behaviors with proper encapsulation.
 */
public abstract class User {
    // Unique identifier for each user
    private final String userId;
    
    // Basic user information
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    
    // Security-related fields
    private String passwordHash; // Store only hashed password
    private boolean isActive;
    private Date lastLoginDate;
    
    // Account management
    private final Date createdDate;
    private Date lastModifiedDate;
    
    // Access control
    private int accessLevel; // Higher number means more privileges
    
    // Instance initialization block
    {
        this.userId = UUID.randomUUID().toString();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.isActive = true;
    }

    /**
     * Constructor with required fields
     */
    public User(String firstName, String lastName, String email, String phoneNumber, 
                String passwordHash, int accessLevel) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.accessLevel = accessLevel;
    }

    // Abstract methods that must be implemented by concrete user types
    public abstract String getUserType();
    public abstract boolean hasPermission(String operation);
    public abstract boolean validateUser();

    // Getters and Setters with proper encapsulation
    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        this.firstName = firstName;
        updateLastModifiedDate();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        this.lastName = lastName;
        updateLastModifiedDate();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
        updateLastModifiedDate();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^\\+?[0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
        updateLastModifiedDate();
    }

    // Password management - only hash is stored
    public boolean verifyPassword(String hashedPassword) {
        return this.passwordHash.equals(hashedPassword);
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        updateLastModifiedDate();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        updateLastModifiedDate();
    }

    public Date getLastLoginDate() {
        return lastLoginDate != null ? new Date(lastLoginDate.getTime()) : null;
    }

    public void updateLastLoginDate() {
        this.lastLoginDate = new Date();
    }

    public Date getCreatedDate() {
        return new Date(createdDate.getTime());
    }

    public Date getLastModifiedDate() {
        return new Date(lastModifiedDate.getTime());
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    protected void setAccessLevel(int accessLevel) {
        if (accessLevel < 0) {
            throw new IllegalArgumentException("Access level cannot be negative");
        }
        this.accessLevel = accessLevel;
        updateLastModifiedDate();
    }

    // Helper method to update the last modified date
    private void updateLastModifiedDate() {
        this.lastModifiedDate = new Date();
    }

    // Get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return String.format(
            "User{id='%s', name='%s %s', email='%s', phone='%s', active=%b, accessLevel=%d}",
            userId, firstName, lastName, email, phoneNumber, isActive, accessLevel
        );
    }
}
