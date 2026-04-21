package com.internship.order_service.dto;

import com.internship.order_service.model.enums.OrderEventStatus;

import java.time.LocalDateTime;

public record OrderEventResponseDto(
        OrderEventStatus status,
        LocalDateTime eventTimestamp
) {}
