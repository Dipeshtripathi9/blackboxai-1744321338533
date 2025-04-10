package com.realestate.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Concrete implementation of User for customer users.
 * Includes specific attributes and behaviors for system customers.
 */
public class CustomerUser extends User {
    // Customer-specific constants
    private static final int CUSTOMER_ACCESS_LEVEL = 10;
    
    // Customer-specific attributes
    private String address;
    private Date dateOfBirth;
    private String preferredContactMethod; // "email" or "phone"
    private List<String> favoriteProperties;
    private List<String> viewingHistory;
    private boolean isVerified;
    private double budget;

    public CustomerUser(String firstName, String lastName, String email, String phoneNumber,
                       String passwordHash, String address, Date dateOfBirth, 
                       String preferredContactMethod, double budget) {
        super(firstName, lastName, email, phoneNumber, passwordHash, CUSTOMER_ACCESS_LEVEL);
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.preferredContactMethod = preferredContactMethod;
        this.budget = budget;
        this.favoriteProperties = new ArrayList<>();
        this.viewingHistory = new ArrayList<>();
        this.isVerified = false;
    }

    @Override
    public String getUserType() {
        return "Customer";
    }

    @Override
    public boolean hasPermission(String operation) {
        // Define basic customer permissions
        switch (operation) {
            case "VIEW_PROPERTY":
            case "FAVORITE_PROPERTY":
            case "REQUEST_VIEWING":
            case "SUBMIT_INQUIRY":
            case "UPDATE_PROFILE":
                return true;
            case "MAKE_OFFER":
                return isVerified; // Only verified customers can make offers
            default:
                return false;
        }
    }

    @Override
    public boolean validateUser() {
        return getEmail() != null &&
               !getEmail().trim().isEmpty() &&
               address != null &&
               !address.trim().isEmpty() &&
               dateOfBirth != null &&
               budget > 0;
    }

    // Customer-specific methods
    public void addToFavorites(String propertyId) {
        if (!favoriteProperties.contains(propertyId)) {
            favoriteProperties.add(propertyId);
        }
    }

    public void removeFromFavorites(String propertyId) {
        favoriteProperties.remove(propertyId);
    }

    public void addToViewingHistory(String propertyId) {
        if (!viewingHistory.contains(propertyId)) {
            viewingHistory.add(propertyId);
        }
    }

    public boolean isWithinBudget(double propertyPrice) {
        return propertyPrice <= budget;
    }

    // Getters and Setters for customer-specific attributes
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth != null ? new Date(dateOfBirth.getTime()) : null;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.after(new Date())) {
            throw new IllegalArgumentException("Invalid date of birth");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        if (!"email".equalsIgnoreCase(preferredContactMethod) && 
            !"phone".equalsIgnoreCase(preferredContactMethod)) {
            throw new IllegalArgumentException("Invalid contact method. Use 'email' or 'phone'");
        }
        this.preferredContactMethod = preferredContactMethod;
    }

    public List<String> getFavoriteProperties() {
        return new ArrayList<>(favoriteProperties); // Return a copy to maintain encapsulation
    }

    public List<String> getViewingHistory() {
        return new ArrayList<>(viewingHistory); // Return a copy to maintain encapsulation
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative");
        }
        this.budget = budget;
    }

    @Override
    public String toString() {
        return String.format("%s, Customer{address='%s', preferredContact='%s', " +
                           "verified=%b, budget=%.2f, favorites=%d, viewings=%d}",
            super.toString(), address, preferredContactMethod, isVerified, 
            budget, favoriteProperties.size(), viewingHistory.size());
    }
}
