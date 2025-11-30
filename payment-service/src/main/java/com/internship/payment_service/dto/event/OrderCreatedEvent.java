package com.internship.payment_service.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String userEmail,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {}