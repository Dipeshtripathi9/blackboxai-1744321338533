package com.realestate.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating different types of properties.
 * Implements the Factory design pattern to demonstrate polymorphism and object creation.
 */
public class PropertyFactory {
    // Property type constants
    public static final String RESIDENTIAL = "RESIDENTIAL";
    public static final String COMMERCIAL = "COMMERCIAL";
    
    // Cache for property prototypes (demonstration of object reuse)
    private static final Map<String, Property> propertyPrototypes = new HashMap<>();
    
    // Static initialization block to set up prototypes
    static {
        // Initialize with default values that will be overwritten
        propertyPrototypes.put(RESIDENTIAL, new ResidentialProperty(
            "Default Residential", "", 0.0, 0.0,
            "", "", "", "",
            0, 0, false, false, "Default"
        ));
        
        propertyPrototypes.put(COMMERCIAL, new CommercialProperty(
            "Default Commercial", "", 0.0, 0.0,
            "", "", "", "",
            "Default", 0, false, 0, "Default"
        ));
    }

    /**
     * Creates a residential property with the specified attributes.
     */
    public static Property createResidentialProperty(
            String title, String description, double price, double area,
            String address, String city, String state, String zipCode,
            int bedrooms, int bathrooms, boolean hasGarage, boolean hasGarden,
            String propertyStyle) {
        
        // Input validation
        validateBasicProperties(title, price, area);
        
        if (bedrooms <= 0) {
            throw new IllegalArgumentException("Number of bedrooms must be positive");
        }
        if (bathrooms <= 0) {
            throw new IllegalArgumentException("Number of bathrooms must be positive");
        }
        
        return new ResidentialProperty(
            title, description, price, area,
            address, city, state, zipCode,
            bedrooms, bathrooms, hasGarage, hasGarden,
            propertyStyle
        );
    }

    /**
     * Creates a commercial property with the specified attributes.
     */
    public static Property createCommercialProperty(
            String title, String description, double price, double area,
            String address, String city, String state, String zipCode,
            String propertyUse, int numberOfUnits, boolean hasParking,
            int parkingSpaces, String zoning) {
        
        // Input validation
        validateBasicProperties(title, price, area);
        
        if (numberOfUnits <= 0) {
            throw new IllegalArgumentException("Number of units must be positive");
        }
        if (hasParking && parkingSpaces <= 0) {
            throw new IllegalArgumentException("Number of parking spaces must be positive when parking is available");
        }
        
        return new CommercialProperty(
            title, description, price, area,
            address, city, state, zipCode,
            propertyUse, numberOfUnits, hasParking,
            parkingSpaces, zoning
        );
    }

    /**
     * Creates a property based on type and a map of attributes.
     * Demonstrates dynamic property creation based on type.
     */
    public static Property createProperty(String type, Map<String, Object> attributes) {
        switch (type.toUpperCase()) {
            case RESIDENTIAL:
                return createResidentialProperty(
                    (String) attributes.get("title"),
                    (String) attributes.get("description"),
                    (Double) attributes.get("price"),
                    (Double) attributes.get("area"),
                    (String) attributes.get("address"),
                    (String) attributes.get("city"),
                    (String) attributes.get("state"),
                    (String) attributes.get("zipCode"),
                    (Integer) attributes.get("bedrooms"),
                    (Integer) attributes.get("bathrooms"),
                    (Boolean) attributes.get("hasGarage"),
                    (Boolean) attributes.get("hasGarden"),
                    (String) attributes.get("propertyStyle")
                );
                
            case COMMERCIAL:
                return createCommercialProperty(
                    (String) attributes.get("title"),
                    (String) attributes.get("description"),
                    (Double) attributes.get("price"),
                    (Double) attributes.get("area"),
                    (String) attributes.get("address"),
                    (String) attributes.get("city"),
                    (String) attributes.get("state"),
                    (String) attributes.get("zipCode"),
                    (String) attributes.get("propertyUse"),
                    (Integer) attributes.get("numberOfUnits"),
                    (Boolean) attributes.get("hasParking"),
                    (Integer) attributes.get("parkingSpaces"),
                    (String) attributes.get("zoning")
                );
                
            default:
                throw new IllegalArgumentException("Unknown property type: " + type);
        }
    }

    /**
     * Creates a clone of a prototype property.
     * Demonstrates prototype-based creation (would need proper clone implementation in property classes).
     */
    public static Property createFromPrototype(String type) {
        Property prototype = propertyPrototypes.get(type.toUpperCase());
        if (prototype == null) {
            throw new IllegalArgumentException("No prototype found for type: " + type);
        }
        // Note: This would need proper clone implementation in property classes
        return prototype; // Currently returns the prototype itself, should return a clone
    }

    /**
     * Validates basic property attributes that are common to all property types.
     */
    private static void validateBasicProperties(String title, double price, double area) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (area <= 0) {
            throw new IllegalArgumentException("Area must be positive");
        }
    }

    /**
     * Registers a new property prototype.
     * Demonstrates prototype pattern extension capability.
     */
    public static void registerPropertyPrototype(String type, Property prototype) {
        if (type == null || prototype == null) {
            throw new IllegalArgumentException("Type and prototype cannot be null");
        }
        propertyPrototypes.put(type.toUpperCase(), prototype);
    }

    /**
     * Checks if a property type is registered.
     */
    public static boolean isPropertyTypeRegistered(String type) {
        return propertyPrototypes.containsKey(type.toUpperCase());
    }
}
