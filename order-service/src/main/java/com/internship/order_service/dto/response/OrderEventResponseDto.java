package com.internship.order_service.dto.response;

import com.internship.order_service.model.enums.OrderEventStatus;

import java.time.LocalDateTime;

public record OrderEventResponseDto(
        OrderEventStatus status,
        LocalDateTime eventTimestamp
) {}

