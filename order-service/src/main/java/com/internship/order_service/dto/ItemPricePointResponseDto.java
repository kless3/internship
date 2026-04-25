package com.internship.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemPricePointResponseDto(
        LocalDateTime timestamp,
        BigDecimal price
) {}
