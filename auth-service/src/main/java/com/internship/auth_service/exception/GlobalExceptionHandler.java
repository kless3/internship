package com.internship.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final static String ERROR = "error";
    private final static String MESSAGE = "message";
    private static final String AUTHENTICATION_FAIL = "AUTHENTICATION_FAILED";
    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";

    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<Map<String, String>> handleAuthServiceException(AuthServiceException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());

        HttpStatus status = determineHttpStatus(e);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DuplicateLoginException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateLoginException(DuplicateLoginException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, String>> handleAuthenticationException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, AUTHENTICATION_FAIL);
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({InvalidTokenException.class, TokenExpiredException.class})
    public ResponseEntity<Map<String, String>> handleTokenException(AuthServiceException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<Map<String, String>> handleUserDisabledException(UserDisabledException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<Map<String, String>> handleUserServiceException(UserServiceException e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, e.getErrorCode());
        response.put(MESSAGE, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR, INTERNAL_SERVER_ERROR);
        response.put(MESSAGE, UNEXPECTED_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private HttpStatus determineHttpStatus(AuthServiceException e) {
        return switch (e.getErrorCode()) {
            case "DUPLICATE_LOGIN" -> HttpStatus.CONFLICT;
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_DISABLED" -> HttpStatus.FORBIDDEN;
            case "AUTHENTICATION_FAILED", "INVALID_TOKEN", "TOKEN_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}