package com.internship.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserServiceRequest(
        @NotBlank(message = "Name is mandatory")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is mandatory")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @NotNull(message = "Birth date is mandatory")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email
) {}