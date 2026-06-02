package com.internship.order_service.dto.response;

import java.math.BigDecimal;

public record ShippingAddressChangeFrequencyResponseDto(
        long totalCreatedOrders,
        long ordersWithAddressChanges,
        BigDecimal changeRatePercent
) {
}

