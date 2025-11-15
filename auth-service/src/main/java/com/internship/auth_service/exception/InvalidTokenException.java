package com.internship.auth_service.exception;

public class InvalidTokenException extends AuthServiceException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, "INVALID_TOKEN");
    }
}