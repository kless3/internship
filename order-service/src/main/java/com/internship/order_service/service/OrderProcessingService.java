package com.internship.order_service.service;

import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.response.OrderResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderProcessingService {

    OrderResponseDto payOrder(Long id);

    List<OrderEventResponseDto> getOrderHistory(Long orderId);

    OrderResponseDto restoreOrderStatusAt(Long id, LocalDateTime date);
}
