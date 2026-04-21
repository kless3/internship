package com.internship.order_service.service.impl;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.*;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.InvalidOrderStatusException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.kafka.OrderEventProducer;
import com.internship.order_service.mapper.ItemMapper;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.ItemRepository;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String ORDER_STATUS_NULL = "Order status cannot be null";
    private static final String FAILED_TO_CREATE_ORDER = "Failed to create order";
    private static final String FAILED_TO_UPDATE_ORDER = "Failed to update order";

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserServiceClient userServiceClient;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {

        try {

            Order order = orderMapper.toEntity(orderRequestDTO);
            order.setStatus(OrderStatus.PENDING);

            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));
            }

            Order savedOrder = orderRepository.save(order);

            BigDecimal totalAmount = calculateTotal(savedOrder);
            OrderEvent savedOrderEvent = saveEvent(savedOrder);

            orderEventProducer.sendOrderCreatedEvent(savedOrderEvent, totalAmount);

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
    @Transactional(readOnly = true)
    public Page<ItemDTO> getAllAvailableItems(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return itemRepository.findAll(pageable)
                .map(itemMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByUserEmail(String userEmail, int page, int size) {
        try {
            UserInfoDTO user = userServiceClient.getUserInfoByEmail(userEmail);

            if (user == null || user.id() == null) {
                throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + userEmail);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
            return orderRepository.findAllByUserId(user.id(), pageable)
                    .map(order -> {
                        OrderResponseDTO orderResponseDTO = orderMapper.toDTO(order);
                        return new OrderResponseDTO(
                                orderResponseDTO.id(),
                                orderResponseDTO.userId(),
                                orderResponseDTO.status(),
                                orderResponseDTO.creationDate(),
                                orderResponseDTO.orderItems(),
                                user
                        );
                    });

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

    @Override
    @Transactional(readOnly = true)
    public List<OrderEventResponseDto> getOrderHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + orderId);
        }

        return orderEventRepository.findAllByOrderIdOrderByEventTimestampAsc(orderId)
                .stream()
                .map(event -> new OrderEventResponseDto(
                        event.getStatus(),
                        event.getEventTimestamp()
                ))
                .toList();
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderEvent saveEvent(Order order) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(order.getId());
        orderEvent.setUserId(order.getUserId());
        orderEvent.setUserEmail(order.getUserEmail());
        orderEvent.setStatus(OrderEventStatus.CREATED);

        return orderEventRepository.save(orderEvent);
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        OrderResponseDTO orderResponseDTO = orderMapper.toDTO(order);
        UserInfoDTO userInfo = userServiceClient.getUserInfoByEmail(order.getUserEmail());
        return new OrderResponseDTO(
                orderResponseDTO.id(),
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
