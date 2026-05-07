package com.example.travelappbe.exception;

public class UserLocationNotFoundException extends RuntimeException {
    public UserLocationNotFoundException(String message) {
        super(message);
    }
}
