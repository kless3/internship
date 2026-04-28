package com.internship.order_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemPricePointResponseDto(
        LocalDateTime timestamp,
        BigDecimal price
) {}

