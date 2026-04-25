package com.internship.order_service.service.impl;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.OrderEventResponseDto;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.dto.UpdateShippingAddressRequestDto;
import com.internship.order_service.dto.UserInfoDTO;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.InvalidOrderStatusException;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.kafka.OrderEventProducer;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
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
import java.time.LocalDateTime;
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
    private static final String RESTORE_TIME_REQUIRED = "Restore timestamp is required";
    private static final String RESTORE_TIME_IN_FUTURE = "Restore timestamp cannot be in the future";
    private static final String NO_HISTORICAL_STATE_AT_TIME = "No historical order state found at timestamp: ";
    private static final String EVENT_STATUS_UNSUPPORTED_FOR_RESTORE = "Unsupported order event status for restore: ";
    private static final String ORDER_STATUS_UNSUPPORTED_FOR_RESTORE_EVENT = "Unsupported order status for restore event: ";
    private static final String FAILED_TO_CREATE_ORDER = "Failed to create order";
    private static final String FAILED_TO_UPDATE_ORDER = "Failed to update order";
    private static final String ONLY_PENDING_ORDER_CAN_BE_PAID = "Only pending orders can be paid";
    private static final String ONLY_PENDING_ORDER_CAN_UPDATE_SHIPPING_ADDRESS = "Shipping address can be updated only while order is pending";
    private static final String FAILED_TO_START_PAYMENT_FOR_ORDER = "Failed to start payment for order";
    private static final String FAILED_TO_UPDATE_SHIPPING_ADDRESS = "Failed to update shipping address";

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderMapper orderMapper;
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
            saveEvent(savedOrder);

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
                                orderResponseDTO.shippingAddress(),
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
    public OrderResponseDTO updateShippingAddress(Long id, UpdateShippingAddressRequestDto requestDto) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new OrderValidationException(ONLY_PENDING_ORDER_CAN_UPDATE_SHIPPING_ADDRESS);
            }

            order.setShippingAddress(requestDto.shippingAddress().trim());
            Order savedOrder = orderRepository.save(order);
            saveEvent(savedOrder, OrderEventStatus.SHIPPING_ADDRESS_UPDATED);

            return toOrderResponseDTO(savedOrder);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (OrderValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_UPDATE_SHIPPING_ADDRESS, e);
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

    @Override
    @Transactional
    public OrderResponseDTO restoreOrderStatusAt(Long id, LocalDateTime date) {
        if (date == null) {
            throw new OrderValidationException(RESTORE_TIME_REQUIRED);
        }
        if (date.isAfter(LocalDateTime.now())) {
            throw new OrderValidationException(RESTORE_TIME_IN_FUTURE);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        OrderEvent historicalEvent = orderEventRepository
                .findTopByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampDesc(id, date)
                .orElseThrow(() -> new OrderValidationException(NO_HISTORICAL_STATE_AT_TIME + date));

        OrderStatus restoredStatus = mapEventStatusToOrderStatus(historicalEvent.getStatus());
        order.setStatus(restoredStatus);
        Order savedOrder = orderRepository.save(order);

        saveEvent(savedOrder, mapOrderStatusToRestoreEventStatus(restoredStatus));

        return toOrderResponseDTO(savedOrder);
    }

    private OrderEvent saveEvent(Order order) {
        return saveEvent(order, OrderEventStatus.CREATED);
    }

    @Override
    @Transactional
    public OrderResponseDTO payOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new OrderValidationException(ONLY_PENDING_ORDER_CAN_BE_PAID);
            }

            order.setStatus(OrderStatus.PROCESSING);
            Order savedOrder = orderRepository.save(order);

            OrderEvent paymentStartedEvent = saveEvent(savedOrder, OrderEventStatus.PAYMENT_STARTED);
            BigDecimal totalAmount = calculateTotal(savedOrder);
            orderEventProducer.sendOrderCreatedEvent(paymentStartedEvent, totalAmount);

            return toOrderResponseDTO(savedOrder);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (OrderValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_START_PAYMENT_FOR_ORDER, e);
        }
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderEvent saveEvent(Order order, OrderEventStatus status) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(order.getId());
        orderEvent.setUserId(order.getUserId());
        orderEvent.setUserEmail(order.getUserEmail());
        orderEvent.setStatus(status);

        return orderEventRepository.save(orderEvent);
    }

    private OrderStatus mapEventStatusToOrderStatus(OrderEventStatus status) {
        return switch (status) {
            case CREATED, SHIPPING_ADDRESS_UPDATED -> OrderStatus.PENDING;
            case PAYMENT_STARTED -> OrderStatus.PROCESSING;
            case PAYMENT_CANCELLED -> OrderStatus.CANCELLED;
            case PAID_SUCCESS, CONFIRMED -> OrderStatus.CONFIRMED;
            case PAID_FAILED, REJECTED -> OrderStatus.FAILED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case REFUNDED -> OrderStatus.REFUNDED;
            default -> throw new OrderValidationException(EVENT_STATUS_UNSUPPORTED_FOR_RESTORE + status);
        };
    }

    private OrderEventStatus mapOrderStatusToRestoreEventStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> OrderEventStatus.CREATED;
            case PROCESSING -> OrderEventStatus.PAYMENT_STARTED;
            case CANCELLED -> OrderEventStatus.PAYMENT_CANCELLED;
            case CONFIRMED -> OrderEventStatus.CONFIRMED;
            case FAILED -> OrderEventStatus.REJECTED;
            case DELIVERED -> OrderEventStatus.DELIVERED;
            case REFUNDED -> OrderEventStatus.REFUNDED;
            default -> throw new OrderValidationException(ORDER_STATUS_UNSUPPORTED_FOR_RESTORE_EVENT + status);
        };
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        OrderResponseDTO orderResponseDTO = orderMapper.toDTO(order);
        UserInfoDTO userInfo = userServiceClient.getUserInfoByEmail(order.getUserEmail());
        return new OrderResponseDTO(
                orderResponseDTO.id(),
                orderResponseDTO.userId(),
                orderResponseDTO.status(),
                orderResponseDTO.creationDate(),
                orderResponseDTO.shippingAddress(),
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
