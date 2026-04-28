package com.internship.order_service.dto.response;

import com.internship.order_service.model.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Long userId,
        OrderStatus status,
        LocalDateTime creationDate,
        String shippingAddress,
        List<OrderItemDto> orderItems,
        UserInfoDto userInfoDto
) {}


