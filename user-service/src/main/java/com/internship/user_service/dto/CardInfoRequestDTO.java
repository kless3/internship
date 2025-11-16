package com.internship.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardInfoRequestDTO(
        @NotBlank(message = "Card number is mandatory")
        @Size(min = 10, max = 20, message = "Number must be between 10 and 20 characters")
        String number,

        @NotBlank(message = "Card holder is mandatory")
        @Size(min = 2, max = 100, message = "Card holder must be between 2 and 100 characters")
        String holder,

        @NotNull(message = "Expiration date is mandatory")
        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {}