package com.internship.auth_service.exception;

public abstract class AuthServiceException extends RuntimeException {
    private final String errorCode;

    public AuthServiceException(String message) {
        super(message);
        this.errorCode = "AUTH_ERROR";
    }

    public AuthServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}