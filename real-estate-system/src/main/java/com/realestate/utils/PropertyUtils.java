package com.realestate.utils;

import com.realestate.exceptions.PropertyException;
import com.realestate.models.Property;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Utility class providing common functionality for property operations.
 * Demonstrates various utility methods and validation patterns.
 */
public class PropertyUtils {
    // Constants for validation
    private static final double MINIMUM_PRICE = 1000.0;
    private static final double MAXIMUM_PRICE = 1_000_000_000.0;
    private static final double MINIMUM_AREA = 100.0;
    private static final int MAXIMUM_TITLE_LENGTH = 100;
    private static final int MAXIMUM_DESCRIPTION_LENGTH = 1000;
    
    // Regular expressions for validation
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    // Currency formatter
    private static final NumberFormat currencyFormatter = 
        NumberFormat.getCurrencyInstance(Locale.US);
    
    // Date formatter
    private static final SimpleDateFormat dateFormatter = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Validates property data
     */
    public static void validateProperty(Property property) throws PropertyException {
        if (property == null) {
            throw new PropertyException("Property cannot be null", 
                PropertyException.INVALID_PROPERTY);
        }

        // Validate title
        if (property.getTitle() == null || property.getTitle().trim().isEmpty()) {
            throw new PropertyException("Property title cannot be empty", 
                PropertyException.VALIDATION_ERROR);
        }
        if (property.getTitle().length() > MAXIMUM_TITLE_LENGTH) {
            throw new PropertyException(
                "Property title exceeds maximum length of " + MAXIMUM_TITLE_LENGTH, 
                PropertyException.VALIDATION_ERROR);
        }

        // Validate description
        if (property.getDescription() != null && 
            property.getDescription().length() > MAXIMUM_DESCRIPTION_LENGTH) {
            throw new PropertyException(
                "Property description exceeds maximum length of " + MAXIMUM_DESCRIPTION_LENGTH, 
                PropertyException.VALIDATION_ERROR);
        }

        // Validate price
        if (property.getPrice() < MINIMUM_PRICE || property.getPrice() > MAXIMUM_PRICE) {
            throw new PropertyException(
                "Property price must be between " + formatCurrency(MINIMUM_PRICE) + 
                " and " + formatCurrency(MAXIMUM_PRICE), 
                PropertyException.VALIDATION_ERROR);
        }

        // Validate area
        if (property.getArea() < MINIMUM_AREA) {
            throw new PropertyException(
                "Property area must be at least " + MINIMUM_AREA + " square feet", 
                PropertyException.VALIDATION_ERROR);
        }

        // Validate address components
        validateAddress(property);
    }

    /**
     * Validates address components
     */
    private static void validateAddress(Property property) throws PropertyException {
        if (property.getAddress() == null || property.getAddress().trim().isEmpty()) {
            throw new PropertyException("Property address cannot be empty", 
                PropertyException.VALIDATION_ERROR);
        }

        if (property.getCity() == null || property.getCity().trim().isEmpty()) {
            throw new PropertyException("Property city cannot be empty", 
                PropertyException.VALIDATION_ERROR);
        }

        if (property.getState() == null || property.getState().trim().isEmpty()) {
            throw new PropertyException("Property state cannot be empty", 
                PropertyException.VALIDATION_ERROR);
        }

        if (!isValidZipCode(property.getZipCode())) {
            throw new PropertyException("Invalid zip code format", 
                PropertyException.VALIDATION_ERROR);
        }
    }

    /**
     * Formats currency values
     */
    public static String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Formats date values
     */
    public static String formatDate(Date date) {
        return date != null ? dateFormatter.format(date) : "";
    }

    /**
     * Calculates price per square foot
     */
    public static double calculatePricePerSquareFoot(double price, double area) {
        if (area <= 0) {
            throw new IllegalArgumentException("Area must be greater than zero");
        }
        return price / area;
    }

    /**
     * Validates zip code format
     */
    public static boolean isValidZipCode(String zipCode) {
        return zipCode != null && ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    /**
     * Validates phone number format
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Calculates mortgage payment
     * @param principal The loan amount
     * @param annualRate Annual interest rate (as a percentage)
     * @param years Loan term in years
     * @return Monthly mortgage payment
     */
    public static double calculateMortgagePayment(double principal, double annualRate, int years) {
        if (principal <= 0 || annualRate <= 0 || years <= 0) {
            throw new IllegalArgumentException("Invalid mortgage parameters");
        }

        double monthlyRate = annualRate / 12.0 / 100.0;
        int numberOfPayments = years * 12;

        return principal * 
               (monthlyRate * Math.pow(1 + monthlyRate, numberOfPayments)) / 
               (Math.pow(1 + monthlyRate, numberOfPayments) - 1);
    }

    /**
     * Calculates property appreciation
     * @param currentValue Current property value
     * @param annualAppreciationRate Annual appreciation rate (as a percentage)
     * @param years Number of years
     * @return Future property value
     */
    public static double calculateFutureValue(double currentValue, double annualAppreciationRate, int years) {
        if (currentValue <= 0 || annualAppreciationRate <= 0 || years <= 0) {
            throw new IllegalArgumentException("Invalid appreciation parameters");
        }

        return currentValue * Math.pow(1 + (annualAppreciationRate / 100.0), years);
    }

    /**
     * Sanitizes text input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        // Remove any HTML tags and trim
        return input.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Truncates text to specified length
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Generates a property summary
     */
    public static String generatePropertySummary(Property property) {
        if (property == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();
        summary.append(property.getTitle())
               .append(" - ")
               .append(formatCurrency(property.getPrice()))
               .append("\n");
        
        summary.append(property.getArea())
               .append(" sq ft - ")
               .append(formatCurrency(calculatePricePerSquareFoot(
                   property.getPrice(), property.getArea())))
               .append(" per sq ft\n");
        
        summary.append(property.getAddress())
               .append(", ")
               .append(property.getCity())
               .append(", ")
               .append(property.getState())
               .append(" ")
               .append(property.getZipCode());

        return summary.toString();
    }
}
