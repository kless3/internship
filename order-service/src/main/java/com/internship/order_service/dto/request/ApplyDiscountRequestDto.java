package com.internship.order_service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplyDiscountRequestDto(
        @NotNull(message = "Discount percent cannot be null")
        @DecimalMin(value = "0.00", message = "Discount percent must be at least 0")
        @DecimalMax(value = "100.00", message = "Discount percent cannot exceed 100")
        BigDecimal discountPercent
) {}
