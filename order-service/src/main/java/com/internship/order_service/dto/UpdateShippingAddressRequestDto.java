package com.internship.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateShippingAddressRequestDto(
        @NotBlank(message = "Shipping address cannot be blank")
        @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
        String shippingAddress
) {
}
