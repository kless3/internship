package com.internship.auth_service.dto;

public record TokenValidationResponse(
        boolean valid,
        String login,
        String message
) {}