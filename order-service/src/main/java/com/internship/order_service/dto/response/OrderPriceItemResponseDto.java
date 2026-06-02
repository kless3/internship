package com.internship.order_service.dto.response;

import java.math.BigDecimal;

public record OrderPriceItemResponseDto(
        Long itemId,
        String itemName,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
