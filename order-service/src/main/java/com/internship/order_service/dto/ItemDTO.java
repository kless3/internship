package com.internship.order_service.dto;

import java.math.BigDecimal;

public record ItemDTO(
        Long id,
        String name,
        BigDecimal price
) {}