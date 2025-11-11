package com.aarkay.health.exception;

/**
 * Custom exception for FHIR client operations.
 * Thrown when FHIR API calls fail or encounter errors.
 */
public class FhirClientException extends RuntimeException {

    public FhirClientException(String message) {
        super(message);
    }

    public FhirClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
