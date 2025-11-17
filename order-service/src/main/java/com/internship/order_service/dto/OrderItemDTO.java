package com.internship.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDTO(
        @Valid
        @NotNull(message = "Item cannot be null")
        ItemDTO item,

        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        Long quantity
) {}