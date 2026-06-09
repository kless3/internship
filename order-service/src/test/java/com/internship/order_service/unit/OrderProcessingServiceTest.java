package com.internship.order_service.unit;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.response.OrderItemDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.response.UserInfoDto;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.messaging.OrderCreatedEventPublisher;
import com.internship.order_service.model.Item;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.OrderItem;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderEventService;
import com.internship.order_service.service.impl.OrderProcessingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventService orderEventService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderCreatedEventPublisher orderCreatedEventPublisher;

    @InjectMocks
    private OrderProcessingServiceImpl orderProcessingService;

    private Order order;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        Item item = new Item(1L, "Test Item", new BigDecimal("29.99"));
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(2L);

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUserEmail("test@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("Test address");
        order.setCreationDate(LocalDateTime.now());
        order.setOrderItems(List.of(orderItem));

        ItemDto itemDto = new ItemDto(1L, "Test Item", new BigDecimal("29.99"));
        OrderItemDto orderItemDto = new OrderItemDto(itemDto, 2L);
        UserInfoDto userInfoDto = new UserInfoDto(1L, "test@example.com");

        orderResponseDto = new OrderResponseDto(
                1L,
                1L,
                OrderStatus.PROCESSING,
                LocalDateTime.now(),
                "Test address",
                List.of(orderItemDto),
                userInfoDto,
                BigDecimal.ZERO
        );
    }

    @Test
    void payOrder_shouldMoveToProcessingAndSendEvent() {
        OrderEvent savedEvent = new OrderEvent();
        savedEvent.setOrderId(1L);
        savedEvent.setStatus(OrderEventStatus.PAYMENT_STARTED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderEventService.savePaymentStarted(any(Order.class), any(BigDecimal.class))).thenReturn(savedEvent);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderProcessingService.payOrder(1L);

        assertEquals(OrderStatus.PROCESSING, result.status());
        verify(orderEventService).savePaymentStarted(order, new BigDecimal("59.98"));
        verify(orderCreatedEventPublisher).sendOrderCreatedEvent(any(OrderEvent.class), any(BigDecimal.class));
    }

    @Test
    void payOrder_shouldThrowWhenOrderIsNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderValidationException.class, () -> orderProcessingService.payOrder(1L));
    }

    @Test
    void payOrder_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderProcessingService.payOrder(999L));
    }

    @Test
    void getOrderHistory_shouldReturnHistory() {
        OrderEvent e = new OrderEvent();
        e.setStatus(OrderEventStatus.CREATED);
        e.setEventTimestamp(LocalDateTime.now());

        when(orderRepository.existsById(1L)).thenReturn(true);
        when(orderEventService.getOrderHistory(1L)).thenReturn(List.of(e));

        List<OrderEventResponseDto> result = orderProcessingService.getOrderHistory(1L);

        assertEquals(1, result.size());
        assertEquals(OrderEventStatus.CREATED, result.get(0).status());
    }

    @Test
    void getOrderHistory_shouldThrowWhenOrderNotFound() {
        when(orderRepository.existsById(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> orderProcessingService.getOrderHistory(999L));
    }

    @Test
    void restoreOrderStatusAt_shouldRestoreWhenEventFound() {
        OrderEvent historical = new OrderEvent();
        historical.setStatus(OrderEventStatus.PAID_SUCCESS);

        LocalDateTime restoreDate = LocalDateTime.now().minusMinutes(1);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderEventService.getOrderHistoryUntil(1L, restoreDate)).thenReturn(List.of(historical));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderProcessingService.restoreOrderStatusAt(1L, restoreDate);

        assertEquals(1L, result.id());
        verify(orderEventService).saveRestored(order, restoreDate);
    }

    @Test
    void restoreOrderStatusAt_shouldThrowWhenDateNull() {
        assertThrows(OrderValidationException.class, () -> orderProcessingService.restoreOrderStatusAt(1L, null));
    }

    @Test
    void restoreOrderStatusAt_shouldThrowWhenDateInFuture() {
        assertThrows(OrderValidationException.class,
                () -> orderProcessingService.restoreOrderStatusAt(1L, LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void restoreOrderStatusAt_shouldThrowWhenNoHistoricalEvent() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderEventService.getOrderHistoryUntil(any(), any())).thenReturn(List.of());

        assertThrows(OrderValidationException.class,
                () -> orderProcessingService.restoreOrderStatusAt(1L, LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void applyDiscount_shouldSaveDiscountAppliedEvent() {
        BigDecimal discountPercent = new BigDecimal("15.00");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderProcessingService.applyDiscount(1L, discountPercent);

        assertEquals(1L, result.id());
        assertEquals(discountPercent, order.getDiscountPercent());
        verify(orderEventService).saveDiscountChanged(order, discountPercent, OrderEventStatus.DISCOUNT_APPLIED);
    }

    @Test
    void applyDiscount_shouldSaveDiscountRemovedEventWhenDiscountIsZero() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderProcessingService.applyDiscount(1L, BigDecimal.ZERO);

        assertEquals(1L, result.id());
        assertEquals(BigDecimal.ZERO, order.getDiscountPercent());
        verify(orderEventService).saveDiscountChanged(order, BigDecimal.ZERO, OrderEventStatus.DISCOUNT_REMOVED);
    }

    @Test
    void applyDiscount_shouldThrowWhenOrderIsNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderValidationException.class,
                () -> orderProcessingService.applyDiscount(1L, new BigDecimal("10.00")));
    }
}
