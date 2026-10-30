package com.project.banksphere.exception;

/**
 * Thrown when a requested resource is not found in the persistence layer.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
