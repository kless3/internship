package com.internship.auth_service.exception;

public class UserNotFoundException extends AuthServiceException {
    public UserNotFoundException(String login) {
        super("User with login '" + login + "' not found", "USER_NOT_FOUND");
    }

    public UserNotFoundException(Long userId) {
        super("User with id '" + userId + "' not found", "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause, "USER_NOT_FOUND");
    }
}