package com.internship.order_service.dto;

import com.internship.order_service.model.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long userId,
        OrderStatus status,
        LocalDateTime creationDate,
        List<OrderItemDTO> orderItems,
        UserInfoDTO userInfoDto
) {}