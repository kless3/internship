package com.internship.order_service.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDto(
        @Valid
        @NotNull(message = "Item cannot be null")
        ItemDto item,

        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        Long quantity
) {}
