package com.internship.auth_service.exception;

public class DuplicateLoginException extends AuthServiceException {
    public DuplicateLoginException(String login) {
        super("Login '" + login + "' already exists", "DUPLICATE_LOGIN");
    }

    public DuplicateLoginException(String login, Throwable cause) {
        super("Login '" + login + "' already exists", cause, "DUPLICATE_LOGIN");
    }
}