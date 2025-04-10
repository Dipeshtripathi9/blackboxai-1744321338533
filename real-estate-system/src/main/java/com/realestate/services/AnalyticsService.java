package com.realestate.services;

import com.realestate.models.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service class for property analytics and statistics.
 * Demonstrates data processing, analysis, and reporting capabilities.
 */
public class AnalyticsService {
    // Singleton instance
    private static AnalyticsService instance;
    
    // Services
    private final PropertyService propertyService;
    private final TransactionService transactionService;
    
    // Cache for analytics results
    private final Map<String, AnalyticsResult> analyticsCache;
    private static final int CACHE_EXPIRY_HOURS = 24;
    
    // Thread pool for parallel processing
    private final ExecutorService analyticsExecutor;
    
    /**
     * Inner class to represent market trends
     */
    public static class MarketTrends {
        private final double averagePrice;
        private final double priceChangePercent;
        private final double averageDaysOnMarket;
        private final int totalListings;
        private final int newListings;
        private final Map<String, Integer> listingsByType;
        private final Map<String, Double> averagePriceByType;

        public MarketTrends(double averagePrice, double priceChangePercent,
                           double averageDaysOnMarket, int totalListings,
                           int newListings, Map<String, Integer> listingsByType,
                           Map<String, Double> averagePriceByType) {
            this.averagePrice = averagePrice;
            this.priceChangePercent = priceChangePercent;
            this.averageDaysOnMarket = averageDaysOnMarket;
            this.totalListings = totalListings;
            this.newListings = newListings;
            this.listingsByType = listingsByType;
            this.averagePriceByType = averagePriceByType;
        }

        // Getters
        public double getAveragePrice() { return averagePrice; }
        public double getPriceChangePercent() { return priceChangePercent; }
        public double getAverageDaysOnMarket() { return averageDaysOnMarket; }
        public int getTotalListings() { return totalListings; }
        public int getNewListings() { return newListings; }
        public Map<String, Integer> getListingsByType() { return listingsByType; }
        public Map<String, Double> getAveragePriceByType() { return averagePriceByType; }
    }

    /**
     * Inner class to represent property performance metrics
     */
    public static class PropertyMetrics {
        private final String propertyId;
        private final int viewCount;
        private final int favoriteCount;
        private final int inquiryCount;
        private final double priceHistory;
        private final int daysOnMarket;
        private final List<String> similarProperties;

        public PropertyMetrics(String propertyId, int viewCount, int favoriteCount,
                             int inquiryCount, double priceHistory, int daysOnMarket,
                             List<String> similarProperties) {
            this.propertyId = propertyId;
            this.viewCount = viewCount;
            this.favoriteCount = favoriteCount;
            this.inquiryCount = inquiryCount;
            this.priceHistory = priceHistory;
            this.daysOnMarket = daysOnMarket;
            this.similarProperties = similarProperties;
        }

        // Getters
        public String getPropertyId() { return propertyId; }
        public int getViewCount() { return viewCount; }
        public int getFavoriteCount() { return favoriteCount; }
        public int getInquiryCount() { return inquiryCount; }
        public double getPriceHistory() { return priceHistory; }
        public int getDaysOnMarket() { return daysOnMarket; }
        public List<String> getSimilarProperties() { return similarProperties; }
    }

    /**
     * Inner class to represent analytics results
     */
    private static class AnalyticsResult {
        private final Object data;
        private final LocalDateTime timestamp;

        public AnalyticsResult(Object data) {
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        public Object getData() { return data; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // Private constructor for singleton pattern
    private AnalyticsService() {
        this.propertyService = PropertyService.getInstance();
        this.transactionService = TransactionService.getInstance();
        this.analyticsCache = new ConcurrentHashMap<>();
        this.analyticsExecutor = Executors.newFixedThreadPool(3);
        
        // Start cache cleanup
        startCacheCleanup();
    }

    /**
     * Gets the singleton instance
     */
    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }

    /**
     * Gets market trends for a specific location
     */
    public MarketTrends getMarketTrends(String city, String state) {
        String cacheKey = "trends_" + city + "_" + state;
        
        AnalyticsResult cached = analyticsCache.get(cacheKey);
        if (cached != null && !isCacheExpired(cached)) {
            return (MarketTrends) cached.getData();
        }

        // Get all properties in the location
        List<Property> properties = propertyService.searchProperties(Map.of(
            "city", city,
            "state", state
        ));

        // Calculate metrics
        double averagePrice = calculateAveragePrice(properties);
        double priceChange = calculatePriceChange(properties);
        double avgDaysOnMarket = calculateAverageDaysOnMarket(properties);
        int totalListings = properties.size();
        int newListings = countNewListings(properties);
        Map<String, Integer> listingsByType = categorizeListings(properties);
        Map<String, Double> avgPriceByType = calculateAveragePriceByType(properties);

        MarketTrends trends = new MarketTrends(
            averagePrice, priceChange, avgDaysOnMarket,
            totalListings, newListings, listingsByType, avgPriceByType
        );

        // Cache results
        analyticsCache.put(cacheKey, new AnalyticsResult(trends));

        return trends;
    }

    /**
     * Gets performance metrics for a specific property
     */
    public PropertyMetrics getPropertyMetrics(String propertyId) {
        String cacheKey = "metrics_" + propertyId;
        
        AnalyticsResult cached = analyticsCache.get(cacheKey);
        if (cached != null && !isCacheExpired(cached)) {
            return (PropertyMetrics) cached.getData();
        }

        Property property = propertyService.getProperty(propertyId);
        
        // Calculate metrics (in real system, these would come from actual tracking data)
        int viewCount = calculateViewCount(propertyId);
        int favoriteCount = calculateFavoriteCount(propertyId);
        int inquiryCount = calculateInquiryCount(propertyId);
        double priceHistory = calculatePriceHistory(propertyId);
        int daysOnMarket = calculateDaysOnMarket(property);
        List<String> similarProperties = findSimilarProperties(property);

        PropertyMetrics metrics = new PropertyMetrics(
            propertyId, viewCount, favoriteCount, inquiryCount,
            priceHistory, daysOnMarket, similarProperties
        );

        // Cache results
        analyticsCache.put(cacheKey, new AnalyticsResult(metrics));

        return metrics;
    }

    /**
     * Generates a price prediction for a property
     */
    public double predictPropertyPrice(Property property) {
        // In a real system, this would use machine learning models
        // Here we'll use a simple calculation based on similar properties
        List<Property> similarProperties = propertyService.searchProperties(Map.of(
            "city", property.getCity(),
            "state", property.getState(),
            "type", property.getPropertyType()
        ));

        return similarProperties.stream()
            .mapToDouble(Property::getPrice)
            .average()
            .orElse(0.0);
    }

    // Private helper methods

    private double calculateAveragePrice(List<Property> properties) {
        return properties.stream()
            .mapToDouble(Property::getPrice)
            .average()
            .orElse(0.0);
    }

    private double calculatePriceChange(List<Property> properties) {
        // In a real system, this would compare with historical data
        return 0.0;
    }

    private double calculateAverageDaysOnMarket(List<Property> properties) {
        return properties.stream()
            .mapToInt(p -> calculateDaysOnMarket(p))
            .average()
            .orElse(0.0);
    }

    private int countNewListings(List<Property> properties) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        return (int) properties.stream()
            .filter(p -> p.getCreatedDate().after(java.sql.Timestamp.valueOf(oneWeekAgo)))
            .count();
    }

    private Map<String, Integer> categorizeListings(List<Property> properties) {
        return properties.stream()
            .collect(Collectors.groupingBy(
                Property::getPropertyType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    private Map<String, Double> calculateAveragePriceByType(List<Property> properties) {
        return properties.stream()
            .collect(Collectors.groupingBy(
                Property::getPropertyType,
                Collectors.averagingDouble(Property::getPrice)
            ));
    }

    private int calculateViewCount(String propertyId) {
        // In a real system, this would come from tracking data
        return new Random().nextInt(100);
    }

    private int calculateFavoriteCount(String propertyId) {
        // In a real system, this would come from user data
        return new Random().nextInt(50);
    }

    private int calculateInquiryCount(String propertyId) {
        // In a real system, this would come from inquiry tracking
        return new Random().nextInt(20);
    }

    private double calculatePriceHistory(String propertyId) {
        // In a real system, this would analyze price changes
        return 0.0;
    }

    private int calculateDaysOnMarket(Property property) {
        return (int) ChronoUnit.DAYS.between(
            property.getCreatedDate().toInstant(),
            new Date().toInstant()
        );
    }

    private List<String> findSimilarProperties(Property property) {
        // Find properties with similar characteristics
        return propertyService.searchProperties(Map.of(
            "city", property.getCity(),
            "type", property.getPropertyType()
        )).stream()
            .map(Property::getPropertyId)
            .limit(5)
            .collect(Collectors.toList());
    }

    private boolean isCacheExpired(AnalyticsResult result) {
        return ChronoUnit.HOURS.between(result.getTimestamp(), LocalDateTime.now()) 
            > CACHE_EXPIRY_HOURS;
    }

    private void startCacheCleanup() {
        Thread cleanup = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(CACHE_EXPIRY_HOURS * 60 * 60 * 1000);
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

    private void cleanupCache() {
        analyticsCache.entrySet().removeIf(entry -> 
            isCacheExpired(entry.getValue())
        );
    }
}
