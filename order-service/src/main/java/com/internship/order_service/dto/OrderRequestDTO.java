package com.internship.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotBlank(message = "User email cannot be blank")
        @Email(message = "User email should be valid")
        String userEmail,

        @Valid
        @NotNull(message = "Order items cannot be null")
        @Size(min = 1, message = "Order must contain at least one item")
        List<OrderItemDTO> orderItems
) {}