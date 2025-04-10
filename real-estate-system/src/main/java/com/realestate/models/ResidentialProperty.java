package com.realestate.models;

/**
 * Concrete implementation of Property for residential properties.
 * Includes specific attributes and behaviors for residential properties.
 */
public class ResidentialProperty extends Property {
    private int bedrooms;
    private int bathrooms;
    private boolean hasGarage;
    private boolean hasGarden;
    private String propertyStyle; // e.g., "Apartment", "House", "Villa"
    
    // Tax rate for residential properties (can be modified based on requirements)
    private static final double RESIDENTIAL_TAX_RATE = 0.02; // 2%

    public ResidentialProperty(String title, String description, double price, double area,
                             String address, String city, String state, String zipCode,
                             int bedrooms, int bathrooms, boolean hasGarage, boolean hasGarden,
                             String propertyStyle) {
        super(title, description, price, area, address, city, state, zipCode);
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.hasGarage = hasGarage;
        this.hasGarden = hasGarden;
        this.propertyStyle = propertyStyle;
    }

    @Override
    public double calculateTax() {
        // Basic tax calculation for residential properties
        double baseTax = getPrice() * RESIDENTIAL_TAX_RATE;
        
        // Additional tax for luxury properties (price > 1 million)
        if (getPrice() > 1_000_000) {
            baseTax *= 1.5; // 50% additional tax for luxury properties
        }
        
        return baseTax;
    }

    @Override
    public String getPropertyType() {
        return "Residential";
    }

    @Override
    public boolean validateProperty() {
        // Basic validation rules for residential properties
        return getPrice() > 0 &&
               getArea() > 0 &&
               bedrooms > 0 &&
               bathrooms > 0 &&
               getAddress() != null &&
               !getAddress().trim().isEmpty() &&
               propertyStyle != null &&
               !propertyStyle.trim().isEmpty();
    }

    // Getters and Setters for residential-specific attributes
    public int getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(int bedrooms) {
        if (bedrooms < 0) {
            throw new IllegalArgumentException("Number of bedrooms cannot be negative");
        }
        this.bedrooms = bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        if (bathrooms < 0) {
            throw new IllegalArgumentException("Number of bathrooms cannot be negative");
        }
        this.bathrooms = bathrooms;
    }

    public boolean isHasGarage() {
        return hasGarage;
    }

    public void setHasGarage(boolean hasGarage) {
        this.hasGarage = hasGarage;
    }

    public boolean isHasGarden() {
        return hasGarden;
    }

    public void setHasGarden(boolean hasGarden) {
        this.hasGarden = hasGarden;
    }

    public String getPropertyStyle() {
        return propertyStyle;
    }

    public void setPropertyStyle(String propertyStyle) {
        this.propertyStyle = propertyStyle;
    }

    @Override
    public String toString() {
        return String.format("%s, Residential{bedrooms=%d, bathrooms=%d, hasGarage=%b, hasGarden=%b, style='%s'}",
            super.toString(), bedrooms, bathrooms, hasGarage, hasGarden, propertyStyle);
    }
}
