package com.internship.order_service.service;

import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderLifecycleService {

    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);

    OrderResponseDto getOrderById(Long id);

    List<OrderResponseDto> getOrdersByIds(List<Long> ids);

    List<OrderResponseDto> getOrdersByStatus(OrderStatus orderStatus);

    Page<OrderResponseDto> getOrdersByUserEmail(String userEmail, int page, int size);

    OrderResponseDto updateOrderById(Long id, OrderRequestDto orderRequestDto);

    OrderResponseDto updateShippingAddress(Long id, UpdateShippingAddressRequestDto requestDto);

    void deleteOrderById(Long id);
}
