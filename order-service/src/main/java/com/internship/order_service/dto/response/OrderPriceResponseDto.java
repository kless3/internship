package com.internship.order_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderPriceResponseDto(
        Long orderId,
        LocalDateTime date,
        BigDecimal subtotal,
        BigDecimal discountPercent,
        BigDecimal total,
        List<OrderPriceItemResponseDto> items
) {}
