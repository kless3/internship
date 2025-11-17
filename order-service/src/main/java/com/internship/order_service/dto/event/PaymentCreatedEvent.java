package com.internship.order_service.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCreatedEvent(
        String paymentId,
        Long orderId,
        Long userId,
        String status,
        BigDecimal amount,
        LocalDateTime createdAt
) {}