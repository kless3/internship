package com.internship.order_service.service.impl;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.dto.UserInfoDTO;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.InvalidOrderStatusException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.kafka.OrderEventProducer;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_WITH_ID = "Order not found with id: ";
    private static final String ORDER_NOT_FOUND_WITH_IDS = "Orders not found with ids: ";
    private static final String ORDER_NOT_FOUND_WITH_STATUS = "Orders not found with status: ";
    private static final String USER_SERVICE_UNAVAILABLE = "User service is currently unavailable";
    private static final String ORDER_STATUS_NULL = "Order status cannot be null";
    private static final String FAILED_TO_CREATE_ORDER = "Failed to create order";
    private static final String FAILED_TO_UPDATE_ORDER = "Failed to update order";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        try {

            Order order = orderMapper.toEntity(orderRequestDTO);

            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));
            }

            Order savedOrder = orderRepository.save(order);
            orderEventProducer.sendOrderCreatedEvent(savedOrder);
            return toOrderResponseDTO(savedOrder);

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_CREATE_ORDER, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        try {
            return toOrderResponseDTO(order);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_IDS + ids);
        }

        try {
            Set<String> userEmails = orders.stream()
                    .map(Order::getUserEmail)
                    .collect(Collectors.toSet());

            return orders.stream()
                    .map(this::toOrderResponseDTO)
                    .collect(Collectors.toList());

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus orderStatus) {
        validateOrderStatus(orderStatus);

        List<Order> orders = orderRepository.findByStatus(orderStatus);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_STATUS + orderStatus);
        }

        try {
            return orders.stream()
                    .map(this::toOrderResponseDTO)
                    .collect(Collectors.toList());

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderById(Long id, OrderRequestDTO orderRequestDTO) {
        try {
            Order existingOrder = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            orderMapper.updateEntityFromDTO(orderRequestDTO, existingOrder);
            Order savedOrder = orderRepository.save(existingOrder);

            return toOrderResponseDTO(savedOrder);

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_UPDATE_ORDER, e);
        }
    }

    @Override
    @Transactional
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        orderRepository.deleteById(id);
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        OrderResponseDTO orderResponseDTO = orderMapper.toDTO(order);
        UserInfoDTO userInfo = userServiceClient.getUserInfoByEmail(order.getUserEmail());
        return new OrderResponseDTO(
                orderResponseDTO.userId(),
                orderResponseDTO.status(),
                orderResponseDTO.creationDate(),
                orderResponseDTO.orderItems(),
                userInfo
        );
    }

    private void validateOrderStatus(OrderStatus status) {
        if (status == null) {
            throw new InvalidOrderStatusException(ORDER_STATUS_NULL);
        }
    }
}