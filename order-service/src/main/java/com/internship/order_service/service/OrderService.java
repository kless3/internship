package com.internship.order_service.service;

import com.internship.order_service.dto.ItemDTO;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByIds(List<Long> ids);

    List<OrderResponseDTO> getOrdersByStatus(OrderStatus orderStatus);

    Page<ItemDTO> getAllAvailableItems(int page, int size);

    Page<OrderResponseDTO> getOrdersByUserEmail(String userEmail, int page, int size);

    OrderResponseDTO updateOrderById(Long id, OrderRequestDTO orderRequestDTO);

    void deleteOrderById(Long id);

}
