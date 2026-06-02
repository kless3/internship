package com.internship.order_service.dto.response;

import java.math.BigDecimal;

public record ItemDto(
        Long id,
        String name,
        BigDecimal price
) {}
