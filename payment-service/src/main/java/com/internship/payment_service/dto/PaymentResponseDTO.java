package com.internship.payment_service.dto;

import com.internship.payment_service.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        String id,
        Long orderId,
        Long userId,
        BigDecimal paymentAmount,
        PaymentStatus status,
        LocalDateTime timestamp
) {}