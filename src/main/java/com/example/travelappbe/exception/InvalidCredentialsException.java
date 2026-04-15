package com.example.travelappbe.exception;

/**
 * Exception thrown when authentication fails (invalid credentials)
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
