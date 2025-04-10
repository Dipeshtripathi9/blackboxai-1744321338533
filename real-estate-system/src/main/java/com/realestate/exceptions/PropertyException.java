package com.realestate.exceptions;

/**
 * Custom exception class for property-related errors.
 * Demonstrates custom exception handling in the system.
 */
public class PropertyException extends RuntimeException {
    private final String errorCode;
    private final String propertyId;

    // Error codes
    public static final String INVALID_PROPERTY = "ERR_INVALID_PROPERTY";
    public static final String PROPERTY_NOT_FOUND = "ERR_PROPERTY_NOT_FOUND";
    public static final String DUPLICATE_PROPERTY = "ERR_DUPLICATE_PROPERTY";
    public static final String VALIDATION_ERROR = "ERR_VALIDATION";
    public static final String DATABASE_ERROR = "ERR_DATABASE";
    public static final String UNAUTHORIZED = "ERR_UNAUTHORIZED";

    /**
     * Constructor for general property exceptions
     */
    public PropertyException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.propertyId = null;
    }

    /**
     * Constructor for property-specific exceptions
     */
    public PropertyException(String message, String errorCode, String propertyId) {
        super(message);
        this.errorCode = errorCode;
        this.propertyId = propertyId;
    }

    /**
     * Constructor with cause
     */
    public PropertyException(String message, String errorCode, String propertyId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.propertyId = propertyId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getPropertyId() {
        return propertyId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertyException: [").append(errorCode).append("] ");
        sb.append(getMessage());
        if (propertyId != null) {
            sb.append(" (Property ID: ").append(propertyId).append(")");
        }
        return sb.toString();
    }
}
