package com.realestate.models;

import java.util.Date;
import java.util.UUID;

/**
 * Abstract base class for all property types in the system.
 * Implements core property attributes and behaviors.
 */
public abstract class Property {
    // Unique identifier for each property
    private final String propertyId;
    
    // Basic property details
    private String title;
    private String description;
    private double price;
    private double area;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    
    // Property status flags
    private boolean isAvailable;
    private boolean isFeatured;
    
    // Timestamps
    private final Date createdDate;
    private Date lastModifiedDate;

    // Instance initialization block
    {
        this.propertyId = UUID.randomUUID().toString();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.isAvailable = true;
        this.isFeatured = false;
    }

    /**
     * Constructor with required fields
     */
    public Property(String title, String description, double price, double area, 
                   String address, String city, String state, String zipCode) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.area = area;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    // Abstract methods that must be implemented by concrete property types
    public abstract double calculateTax();
    public abstract String getPropertyType();
    public abstract boolean validateProperty();

    // Getters and Setters with proper encapsulation
    public String getPropertyId() {
        return propertyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateLastModifiedDate();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModifiedDate();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
        updateLastModifiedDate();
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        if (area <= 0) {
            throw new IllegalArgumentException("Area must be greater than 0");
        }
        this.area = area;
        updateLastModifiedDate();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        updateLastModifiedDate();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        updateLastModifiedDate();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        updateLastModifiedDate();
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
        updateLastModifiedDate();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
        updateLastModifiedDate();
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        this.isFeatured = featured;
        updateLastModifiedDate();
    }

    public Date getCreatedDate() {
        return new Date(createdDate.getTime());
    }

    public Date getLastModifiedDate() {
        return new Date(lastModifiedDate.getTime());
    }

    // Helper method to update the last modified date
    private void updateLastModifiedDate() {
        this.lastModifiedDate = new Date();
    }

    // Calculate price per square foot
    public double getPricePerSquareFoot() {
        return price / area;
    }

    @Override
    public String toString() {
        return String.format(
            "Property{id='%s', title='%s', price=%.2f, area=%.2f, address='%s', city='%s', state='%s', zipCode='%s', available=%b}",
            propertyId, title, price, area, address, city, state, zipCode, isAvailable
        );
    }
}
