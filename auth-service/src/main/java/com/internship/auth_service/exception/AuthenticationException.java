package com.internship.auth_service.exception;

public class AuthenticationException extends AuthServiceException {
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, "AUTHENTICATION_FAILED");
    }
}