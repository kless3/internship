package com.internship.order_service.service.impl;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.response.UserInfoDto;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.messaging.OrderCreatedEventPublisher;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.model.payload.OrderEventPayload;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderEventService;
import com.internship.order_service.service.OrderProcessingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private static final String ORDER_NOT_FOUND_WITH_ID = "Order not found with id: ";
    private static final String USER_SERVICE_UNAVAILABLE = "User service is currently unavailable";
    private static final String RESTORE_TIME_REQUIRED = "Restore timestamp is required";
    private static final String RESTORE_TIME_IN_FUTURE = "Restore timestamp cannot be in the future";
    private static final String NO_HISTORICAL_STATE_AT_TIME = "No historical order state found at timestamp: ";
    private static final String ONLY_PENDING_ORDER_CAN_BE_PAID = "Only pending orders can be paid";
    private static final String FAILED_TO_START_PAYMENT_FOR_ORDER = "Failed to start payment for order";
    private static final String DISCOUNT_ONLY_FOR_PENDING_ORDERS = "Discount can only be applied to pending orders";

    private final OrderRepository orderRepository;
    private final OrderEventService orderEventService;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final OrderCreatedEventPublisher orderCreatedEventPublisher;

    @Override
    public OrderResponseDto payOrder(Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new OrderValidationException(ONLY_PENDING_ORDER_CAN_BE_PAID);
            }

            order.setStatus(OrderStatus.PROCESSING);
            Order savedOrder = orderRepository.save(order);

            BigDecimal totalAmount = calculateTotal(savedOrder);
            OrderEvent paymentStartedEvent = orderEventService.savePaymentStarted(savedOrder, totalAmount);
            orderCreatedEventPublisher.sendOrderCreatedEvent(paymentStartedEvent, totalAmount);

            return toOrderResponseDTO(savedOrder);

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (OrderValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_START_PAYMENT_FOR_ORDER, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderEventResponseDto> getOrderHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + orderId);
        }

        return orderEventService.getOrderHistory(orderId)
                .stream()
                .map(event -> new OrderEventResponseDto(
                        event.getStatus(),
                        event.getEventTimestamp()
                ))
                .toList();
    }

    @Override
    public OrderResponseDto restoreOrderStatusAt(Long id, LocalDateTime date) {

        if (date == null) {
            throw new OrderValidationException(RESTORE_TIME_REQUIRED);
        }
        if (date.isAfter(LocalDateTime.now())) {
            throw new OrderValidationException(RESTORE_TIME_IN_FUTURE);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        List<OrderEvent> history = orderEventService.getOrderHistoryUntil(id, date);

        if (history.isEmpty()) {
            throw new OrderValidationException(NO_HISTORICAL_STATE_AT_TIME + date);
        }

        replayEvents(order, history);
        Order savedOrder = orderRepository.save(order);

        orderEventService.saveRestored(savedOrder, date);

        return toOrderResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDto applyDiscount(Long id, BigDecimal discountPercent) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderValidationException(DISCOUNT_ONLY_FOR_PENDING_ORDERS);
        }

        order.setDiscountPercent(discountPercent);
        Order savedOrder = orderRepository.save(order);

        OrderEventStatus eventStatus;

        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            eventStatus = OrderEventStatus.DISCOUNT_REMOVED;
        } else {
            eventStatus = OrderEventStatus.DISCOUNT_APPLIED;
        }
        orderEventService.saveDiscountChanged(savedOrder, discountPercent, eventStatus);

        return toOrderResponseDTO(savedOrder);
    }

    private void replayEvents(Order order, List<OrderEvent> events) {
        for (OrderEvent event : events) {
            applyEvent(order, event);
        }
    }

    private void applyEvent(Order order, OrderEvent event) {
        switch (event.getStatus()) {
            case CREATED, SHIPPING_ADDRESS_UPDATED -> {
                order.setStatus(OrderStatus.PENDING);
                String shippingAddress = OrderEventPayload.getShippingAddress(event);
                if (shippingAddress != null) {
                    order.setShippingAddress(shippingAddress);
                }
            }
            case PAYMENT_STARTED -> order.setStatus(OrderStatus.PROCESSING);
            case PAYMENT_CANCELLED -> order.setStatus(OrderStatus.CANCELLED);
            case PAID_SUCCESS, CONFIRMED -> order.setStatus(OrderStatus.CONFIRMED);
            case PAID_FAILED, REJECTED -> order.setStatus(OrderStatus.FAILED);
            case DELIVERED -> order.setStatus(OrderStatus.DELIVERED);
            case REFUNDED -> order.setStatus(OrderStatus.REFUNDED);
            case DISCOUNT_APPLIED, DISCOUNT_REMOVED -> order.setDiscountPercent(OrderEventPayload.getDiscountPercent(event));
            case RESTORED -> {}
        }
    }

    private OrderResponseDto toOrderResponseDTO(Order order) {
        OrderResponseDto orderResponseDto = orderMapper.toDTO(order);
        UserInfoDto userInfo = userServiceClient.getUserInfoByEmail(order.getUserEmail());
        return new OrderResponseDto(
                orderResponseDto.id(),
                orderResponseDto.userId(),
                orderResponseDto.status(),
                orderResponseDto.creationDate(),
                orderResponseDto.shippingAddress(),
                orderResponseDto.orderItems(),
                userInfo,
                orderResponseDto.discountPercent()
        );
    }

    private BigDecimal calculateTotal(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getItem().getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
