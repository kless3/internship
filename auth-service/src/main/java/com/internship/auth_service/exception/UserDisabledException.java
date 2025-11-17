package com.internship.auth_service.exception;

public class UserDisabledException extends AuthServiceException {
    public UserDisabledException(String login) {
        super("User account '" + login + "' is disabled", "USER_DISABLED");
    }

    public UserDisabledException(String message, Throwable cause) {
        super(message, cause, "USER_DISABLED");
    }
}