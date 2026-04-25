package com.internship.order_service.service;

import com.internship.order_service.dto.OrderEventResponseDto;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.dto.UpdateShippingAddressRequestDto;
import com.internship.order_service.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO payOrder(Long id);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByIds(List<Long> ids);

    List<OrderResponseDTO> getOrdersByStatus(OrderStatus orderStatus);

    Page<OrderResponseDTO> getOrdersByUserEmail(String userEmail, int page, int size);

    OrderResponseDTO updateOrderById(Long id, OrderRequestDTO orderRequestDTO);

    OrderResponseDTO updateShippingAddress(Long id, UpdateShippingAddressRequestDto requestDto);

    void deleteOrderById(Long id);

    List<OrderEventResponseDto> getOrderHistory(Long orderId);

    OrderResponseDTO restoreOrderStatusAt(Long id, LocalDateTime date);
}
