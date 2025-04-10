package com.realestate.services;

import com.realestate.models.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Service class for property search functionality.
 * Demonstrates streams, lambda expressions, and search algorithms.
 */
public class SearchService {
    // Singleton instance
    private static SearchService instance;
    
    // Services
    private final PropertyService propertyService;
    
    // Search cache
    private final Map<String, SearchResult> searchCache;
    private static final int CACHE_EXPIRY_MINUTES = 15;
    
    // Search result sorting options
    public enum SortOption {
        PRICE_ASC,
        PRICE_DESC,
        DATE_LISTED_ASC,
        DATE_LISTED_DESC,
        AREA_ASC,
        AREA_DESC
    }

    /**
     * Inner class to represent search criteria
     */
    public static class SearchCriteria {
        private Double minPrice;
        private Double maxPrice;
        private Double minArea;
        private Double maxArea;
        private String city;
        private String state;
        private String propertyType;
        private Integer minBedrooms;  // For residential
        private Integer minBathrooms; // For residential
        private String propertyUse;   // For commercial
        private Boolean hasParking;
        private SortOption sortBy;
        private int pageSize;
        private int pageNumber;

        public SearchCriteria() {
            this.pageSize = 10;
            this.pageNumber = 1;
            this.sortBy = SortOption.DATE_LISTED_DESC;
        }

        // Builder pattern setters
        public SearchCriteria withPriceRange(Double min, Double max) {
            this.minPrice = min;
            this.maxPrice = max;
            return this;
        }

        public SearchCriteria withAreaRange(Double min, Double max) {
            this.minArea = min;
            this.maxArea = max;
            return this;
        }

        public SearchCriteria withLocation(String city, String state) {
            this.city = city;
            this.state = state;
            return this;
        }

        public SearchCriteria withPropertyType(String type) {
            this.propertyType = type;
            return this;
        }

        public SearchCriteria withResidentialCriteria(Integer beds, Integer baths) {
            this.minBedrooms = beds;
            this.minBathrooms = baths;
            return this;
        }

        public SearchCriteria withCommercialCriteria(String use, Boolean parking) {
            this.propertyUse = use;
            this.hasParking = parking;
            return this;
        }

        public SearchCriteria withSorting(SortOption sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public SearchCriteria withPagination(int pageSize, int pageNumber) {
            this.pageSize = pageSize;
            this.pageNumber = pageNumber;
            return this;
        }

        // Getters
        public Double getMinPrice() { return minPrice; }
        public Double getMaxPrice() { return maxPrice; }
        public Double getMinArea() { return minArea; }
        public Double getMaxArea() { return maxArea; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getPropertyType() { return propertyType; }
        public Integer getMinBedrooms() { return minBedrooms; }
        public Integer getMinBathrooms() { return minBathrooms; }
        public String getPropertyUse() { return propertyUse; }
        public Boolean getHasParking() { return hasParking; }
        public SortOption getSortBy() { return sortBy; }
        public int getPageSize() { return pageSize; }
        public int getPageNumber() { return pageNumber; }
    }

    /**
     * Inner class to represent search results
     */
    public static class SearchResult {
        private final List<Property> properties;
        private final int totalResults;
        private final int pageSize;
        private final int pageNumber;
        private final LocalDateTime timestamp;

        public SearchResult(List<Property> properties, int total, int pageSize, int pageNumber) {
            this.properties = properties;
            this.totalResults = total;
            this.pageSize = pageSize;
            this.pageNumber = pageNumber;
            this.timestamp = LocalDateTime.now();
        }

        public List<Property> getProperties() { return properties; }
        public int getTotalResults() { return totalResults; }
        public int getPageSize() { return pageSize; }
        public int getPageNumber() { return pageNumber; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // Private constructor for singleton pattern
    private SearchService() {
        this.propertyService = PropertyService.getInstance();
        this.searchCache = new ConcurrentHashMap<>();
        
        // Start cache cleanup thread
        startCacheCleanup();
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }

    /**
     * Performs a property search based on criteria
     */
    public SearchResult searchProperties(SearchCriteria criteria) {
        // Generate cache key
        String cacheKey = generateCacheKey(criteria);
        
        // Check cache
        SearchResult cachedResult = searchCache.get(cacheKey);
        if (cachedResult != null && !isCacheExpired(cachedResult)) {
            return cachedResult;
        }

        // Get all properties and apply filters
        List<Property> allProperties = propertyService.searchProperties(new HashMap<>());
        
        // Apply filters
        List<Property> filteredProperties = allProperties.stream()
            .filter(buildPropertyFilter(criteria))
            .sorted(buildPropertyComparator(criteria.getSortBy()))
            .collect(Collectors.toList());

        // Apply pagination
        int start = (criteria.getPageNumber() - 1) * criteria.getPageSize();
        int end = Math.min(start + criteria.getPageSize(), filteredProperties.size());
        
        List<Property> pagedProperties = filteredProperties.subList(start, end);
        
        // Create result
        SearchResult result = new SearchResult(
            pagedProperties,
            filteredProperties.size(),
            criteria.getPageSize(),
            criteria.getPageNumber()
        );

        // Cache result
        searchCache.put(cacheKey, result);
        
        return result;
    }

    /**
     * Builds a predicate for filtering properties
     */
    private Predicate<Property> buildPropertyFilter(SearchCriteria criteria) {
        List<Predicate<Property>> filters = new ArrayList<>();

        // Add basic filters
        if (criteria.getMinPrice() != null) {
            filters.add(p -> p.getPrice() >= criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            filters.add(p -> p.getPrice() <= criteria.getMaxPrice());
        }
        if (criteria.getMinArea() != null) {
            filters.add(p -> p.getArea() >= criteria.getMinArea());
        }
        if (criteria.getMaxArea() != null) {
            filters.add(p -> p.getArea() <= criteria.getMaxArea());
        }
        if (criteria.getCity() != null) {
            filters.add(p -> p.getCity().equalsIgnoreCase(criteria.getCity()));
        }
        if (criteria.getState() != null) {
            filters.add(p -> p.getState().equalsIgnoreCase(criteria.getState()));
        }

        // Add type-specific filters
        if ("Residential".equalsIgnoreCase(criteria.getPropertyType())) {
            filters.add(p -> p instanceof ResidentialProperty);
            if (criteria.getMinBedrooms() != null) {
                filters.add(p -> ((ResidentialProperty) p).getBedrooms() >= criteria.getMinBedrooms());
            }
            if (criteria.getMinBathrooms() != null) {
                filters.add(p -> ((ResidentialProperty) p).getBathrooms() >= criteria.getMinBathrooms());
            }
        } else if ("Commercial".equalsIgnoreCase(criteria.getPropertyType())) {
            filters.add(p -> p instanceof CommercialProperty);
            if (criteria.getPropertyUse() != null) {
                filters.add(p -> ((CommercialProperty) p).getPropertyUse().equalsIgnoreCase(criteria.getPropertyUse()));
            }
            if (criteria.getHasParking() != null) {
                filters.add(p -> ((CommercialProperty) p).isHasParking() == criteria.getHasParking());
            }
        }

        // Combine all filters
        return filters.stream().reduce(x -> true, Predicate::and);
    }

    /**
     * Builds a comparator for sorting properties
     */
    private Comparator<Property> buildPropertyComparator(SortOption sortBy) {
        switch (sortBy) {
            case PRICE_ASC:
                return Comparator.comparingDouble(Property::getPrice);
            case PRICE_DESC:
                return Comparator.comparingDouble(Property::getPrice).reversed();
            case AREA_ASC:
                return Comparator.comparingDouble(Property::getArea);
            case AREA_DESC:
                return Comparator.comparingDouble(Property::getArea).reversed();
            case DATE_LISTED_ASC:
                return Comparator.comparing(Property::getCreatedDate);
            case DATE_LISTED_DESC:
            default:
                return Comparator.comparing(Property::getCreatedDate).reversed();
        }
    }

    /**
     * Generates a cache key for search criteria
     */
    private String generateCacheKey(SearchCriteria criteria) {
        return String.format("%s_%s_%s_%s_%d_%d",
            criteria.getPropertyType(),
            criteria.getCity(),
            criteria.getState(),
            criteria.getSortBy(),
            criteria.getPageSize(),
            criteria.getPageNumber()
        );
    }

    /**
     * Checks if cached result is expired
     */
    private boolean isCacheExpired(SearchResult result) {
        return Duration.between(result.getTimestamp(), LocalDateTime.now())
                      .toMinutes() > CACHE_EXPIRY_MINUTES;
    }

    /**
     * Starts cache cleanup thread
     */
    private void startCacheCleanup() {
        Thread cleanup = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CACHE_EXPIRY_MINUTES * 60 * 1000);
                    cleanupCache();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanup.setDaemon(true);
        cleanup.start();
    }

    /**
     * Cleans up expired cache entries
     */
    private void cleanupCache() {
        searchCache.entrySet().removeIf(entry -> isCacheExpired(entry.getValue()));
    }
}
