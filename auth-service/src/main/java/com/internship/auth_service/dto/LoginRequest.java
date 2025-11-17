package com.internship.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Login is mandatory")
        @Size(min = 3, max = 100, message = "Login must be between 3 and 100 characters")
        String login,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}