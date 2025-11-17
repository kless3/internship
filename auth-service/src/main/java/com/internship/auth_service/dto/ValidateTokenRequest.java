package com.internship.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
        @NotBlank(message = "Token is mandatory")
        String token
) {}