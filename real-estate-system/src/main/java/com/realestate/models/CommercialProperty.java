package com.realestate.models;

/**
 * Concrete implementation of Property for commercial properties.
 * Includes specific attributes and behaviors for commercial properties.
 */
public class CommercialProperty extends Property {
    private String propertyUse; // e.g., "Office", "Retail", "Industrial"
    private int numberOfUnits;
    private boolean hasParking;
    private int parkingSpaces;
    private String zoning;
    
    // Tax rate for commercial properties (can be modified based on requirements)
    private static final double COMMERCIAL_TAX_RATE = 0.04; // 4%

    public CommercialProperty(String title, String description, double price, double area,
                            String address, String city, String state, String zipCode,
                            String propertyUse, int numberOfUnits, boolean hasParking,
                            int parkingSpaces, String zoning) {
        super(title, description, price, area, address, city, state, zipCode);
        this.propertyUse = propertyUse;
        this.numberOfUnits = numberOfUnits;
        this.hasParking = hasParking;
        this.parkingSpaces = parkingSpaces;
        this.zoning = zoning;
    }

    @Override
    public double calculateTax() {
        // Basic tax calculation for commercial properties
        double baseTax = getPrice() * COMMERCIAL_TAX_RATE;
        
        // Additional tax based on property use
        if ("Retail".equalsIgnoreCase(propertyUse)) {
            baseTax *= 1.2; // 20% additional tax for retail properties
        }
        
        return baseTax;
    }

    @Override
    public String getPropertyType() {
        return "Commercial";
    }

    @Override
    public boolean validateProperty() {
        // Basic validation rules for commercial properties
        return getPrice() > 0 &&
               getArea() > 0 &&
               numberOfUnits > 0 &&
               propertyUse != null &&
               !propertyUse.trim().isEmpty() &&
               zoning != null &&
               !zoning.trim().isEmpty();
    }

    // Getters and Setters for commercial-specific attributes
    public String getPropertyUse() {
        return propertyUse;
    }

    public void setPropertyUse(String propertyUse) {
        this.propertyUse = propertyUse;
    }

    public int getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(int numberOfUnits) {
        if (numberOfUnits < 0) {
            throw new IllegalArgumentException("Number of units cannot be negative");
        }
        this.numberOfUnits = numberOfUnits;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public int getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingSpaces(int parkingSpaces) {
        if (parkingSpaces < 0) {
            throw new IllegalArgumentException("Number of parking spaces cannot be negative");
        }
        this.parkingSpaces = parkingSpaces;
    }

    public String getZoning() {
        return zoning;
    }

    public void setZoning(String zoning) {
        this.zoning = zoning;
    }

    @Override
    public String toString() {
        return String.format("%s, Commercial{use='%s', units=%d, hasParking=%b, parkingSpaces=%d, zoning='%s'}",
            super.toString(), propertyUse, numberOfUnits, hasParking, parkingSpaces, zoning);
    }
}
