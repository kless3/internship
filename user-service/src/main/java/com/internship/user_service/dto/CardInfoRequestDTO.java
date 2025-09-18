package com.internship.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardInfoRequestDTO {

    @NotBlank(message = "Card number is mandatory")
    @Size(min = 10, max = 20, message = "Number must be between 10 and 20 characters")
    private String number;

    @NotBlank(message = "Card holder is mandatory")
    @Size(min = 2, max = 100, message = "Card holder must be between 2 and 100 characters")
    private String holder;

    @NotNull(message = "Expiration date is mandatory")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}