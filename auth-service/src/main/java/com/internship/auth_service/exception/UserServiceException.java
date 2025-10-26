package com.internship.auth_service.exception;

public class UserServiceException extends AuthServiceException{
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public UserServiceException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }
}
