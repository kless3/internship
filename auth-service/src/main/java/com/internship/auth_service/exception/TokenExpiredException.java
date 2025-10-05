package com.internship.auth_service.exception;

public class TokenExpiredException extends AuthServiceException {
    public TokenExpiredException() {
        super("Token has expired", "TOKEN_EXPIRED");
    }

    public TokenExpiredException(String message) {
        super(message, "TOKEN_EXPIRED");
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause, "TOKEN_EXPIRED");
    }
}