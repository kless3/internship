package com.internship.order_service.dto;

import java.math.BigDecimal;

public record AverageCreateToPayDurationResponseDto(
        long samplesCount,
        long averageDurationMs,
        BigDecimal averageDurationSeconds
) {
}
