package com.internship.order_service.service;

import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
import com.internship.order_service.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);

    OrderResponseDto payOrder(Long id);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getOrdersByIds(List<Long> ids);

    List<OrderResponseDto> getOrdersByStatus(OrderStatus orderStatus);

    Page<OrderResponseDto> getOrdersByUserEmail(String userEmail, int page, int size);

    OrderResponseDto updateOrderById(Long id, OrderRequestDto orderRequestDto);

    OrderResponseDto updateShippingAddress(Long id, UpdateShippingAddressRequestDto requestDto);

    void deleteOrderById(Long id);

    List<OrderEventResponseDto> getOrderHistory(Long orderId);

    OrderResponseDto restoreOrderStatusAt(Long id, LocalDateTime date);
}


