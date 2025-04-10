package com.realestate.services;

import com.realestate.models.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class for managing properties in the system.
 * Demonstrates collections, exception handling, and thread safety.
 */
public class PropertyService {
    // Singleton instance
    private static PropertyService instance;
    
    // Thread-safe collection to store properties
    private final Map<String, Property> propertyDatabase;
    
    // Additional indexes for efficient searching
    private final Map<String, Set<String>> cityIndex;
    private final Map<String, Set<String>> typeIndex;
    
    // Property count tracking
    private int totalProperties;
    
    // Constants for validation
    private static final double MIN_PRICE = 1000.0;
    private static final double MAX_PRICE = 1_000_000_000.0;
    private static final double MIN_AREA = 100.0;

    // Private constructor for singleton pattern
    private PropertyService() {
        this.propertyDatabase = new ConcurrentHashMap<>();
        this.cityIndex = new ConcurrentHashMap<>();
        this.typeIndex = new ConcurrentHashMap<>();
        this.totalProperties = 0;
    }

    /**
     * Gets the singleton instance of PropertyService
     */
    public static synchronized PropertyService getInstance() {
        if (instance == null) {
            instance = new PropertyService();
        }
        return instance;
    }

    /**
     * Adds a new property to the system
     */
    public synchronized String addProperty(Property property) {
        try {
            // Validate property
            validateProperty(property);
            
            // Add to main database
            propertyDatabase.put(property.getPropertyId(), property);
            
            // Update indexes
            updateIndexes(property);
            
            // Update counter using compound assignment
            totalProperties += 1;
            
            return property.getPropertyId();
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to add property: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while adding property", e);
        }
    }

    /**
     * Updates an existing property
     */
    public synchronized void updateProperty(String propertyId, Property updatedProperty) {
        try {
            if (!propertyDatabase.containsKey(propertyId)) {
                throw new IllegalArgumentException("Property not found: " + propertyId);
            }
            
            // Validate updated property
            validateProperty(updatedProperty);
            
            // Remove old indexes
            Property oldProperty = propertyDatabase.get(propertyId);
            removeFromIndexes(oldProperty);
            
            // Update property and indexes
            propertyDatabase.put(propertyId, updatedProperty);
            updateIndexes(updatedProperty);
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to update property: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while updating property", e);
        }
    }

    /**
     * Removes a property from the system
     */
    public synchronized boolean removeProperty(String propertyId) {
        try {
            Property property = propertyDatabase.remove(propertyId);
            if (property != null) {
                removeFromIndexes(property);
                totalProperties -= 1; // Decrement counter
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error removing property", e);
        }
    }

    /**
     * Gets a property by ID
     */
    public Property getProperty(String propertyId) {
        Property property = propertyDatabase.get(propertyId);
        if (property == null) {
            throw new IllegalArgumentException("Property not found: " + propertyId);
        }
        return property;
    }

    /**
     * Searches for properties based on various criteria
     */
    public List<Property> searchProperties(Map<String, Object> criteria) {
        try {
            return propertyDatabase.values().stream()
                .filter(property -> matchesCriteria(property, criteria))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error searching properties", e);
        }
    }

    /**
     * Gets properties by city
     */
    public List<Property> getPropertiesByCity(String city) {
        Set<String> propertyIds = cityIndex.getOrDefault(city.toLowerCase(), Collections.emptySet());
        return propertyIds.stream()
            .map(propertyDatabase::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Gets properties by type
     */
    public List<Property> getPropertiesByType(String type) {
        Set<String> propertyIds = typeIndex.getOrDefault(type.toLowerCase(), Collections.emptySet());
        return propertyIds.stream()
            .map(propertyDatabase::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Gets total property count
     */
    public int getTotalProperties() {
        return totalProperties;
    }

    /**
     * Gets properties within a price range
     */
    public List<Property> getPropertiesInPriceRange(double minPrice, double maxPrice) {
        return propertyDatabase.values().stream()
            .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
            .collect(Collectors.toList());
    }

    // Private helper methods

    private void validateProperty(Property property) {
        if (property == null) {
            throw new IllegalArgumentException("Property cannot be null");
        }
        
        if (!property.validateProperty()) {
            throw new IllegalArgumentException("Invalid property data");
        }
        
        if (property.getPrice() < MIN_PRICE || property.getPrice() > MAX_PRICE) {
            throw new IllegalArgumentException("Property price out of valid range");
        }
        
        if (property.getArea() < MIN_AREA) {
            throw new IllegalArgumentException("Property area too small");
        }
    }

    private void updateIndexes(Property property) {
        // Update city index
        cityIndex.computeIfAbsent(property.getCity().toLowerCase(), k -> ConcurrentHashMap.newKeySet())
                .add(property.getPropertyId());
        
        // Update type index
        typeIndex.computeIfAbsent(property.getPropertyType().toLowerCase(), k -> ConcurrentHashMap.newKeySet())
                .add(property.getPropertyId());
    }

    private void removeFromIndexes(Property property) {
        // Remove from city index
        Set<String> cityProperties = cityIndex.get(property.getCity().toLowerCase());
        if (cityProperties != null) {
            cityProperties.remove(property.getPropertyId());
        }
        
        // Remove from type index
        Set<String> typeProperties = typeIndex.get(property.getPropertyType().toLowerCase());
        if (typeProperties != null) {
            typeProperties.remove(property.getPropertyId());
        }
    }

    private boolean matchesCriteria(Property property, Map<String, Object> criteria) {
        for (Map.Entry<String, Object> criterion : criteria.entrySet()) {
            switch (criterion.getKey().toLowerCase()) {
                case "minprice":
                    if (property.getPrice() < (Double) criterion.getValue()) return false;
                    break;
                case "maxprice":
                    if (property.getPrice() > (Double) criterion.getValue()) return false;
                    break;
                case "minarea":
                    if (property.getArea() < (Double) criterion.getValue()) return false;
                    break;
                case "maxarea":
                    if (property.getArea() > (Double) criterion.getValue()) return false;
                    break;
                case "city":
                    if (!property.getCity().equalsIgnoreCase((String) criterion.getValue())) return false;
                    break;
                case "type":
                    if (!property.getPropertyType().equalsIgnoreCase((String) criterion.getValue())) return false;
                    break;
            }
        }
        return true;
    }
}
