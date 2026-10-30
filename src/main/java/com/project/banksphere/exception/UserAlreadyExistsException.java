package com.project.banksphere.exception;

/**
 * Thrown when attempting to register a user with an already registered unique identifier (e.g. email).
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
