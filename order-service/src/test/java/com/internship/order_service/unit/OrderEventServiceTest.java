package com.internship.order_service.unit;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.service.impl.OrderEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventServiceTest {

    @Mock
    private OrderEventRepository orderEventRepository;

    @InjectMocks
    private OrderEventServiceImpl orderEventService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setUserEmail("test@example.com");
        order.setShippingAddress("Test address");
    }

    @Test
    void saveCreated_shouldSaveAddressSnapshotEvent() {
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveCreated(order);

        assertEquals(order.getId(), result.getOrderId());
        assertEquals(order.getUserId(), result.getUserId());
        assertEquals(order.getUserEmail(), result.getUserEmail());
        assertEquals(OrderEventStatus.CREATED, result.getStatus());
        assertEquals("Test address", result.getPayload().get("shippingAddress"));
    }

    @Test
    void saveShippingAddressUpdated_shouldSaveAddressSnapshotEvent() {
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveShippingAddressUpdated(order);

        assertEquals(order.getId(), result.getOrderId());
        assertEquals(OrderEventStatus.SHIPPING_ADDRESS_UPDATED, result.getStatus());
        assertEquals("Test address", result.getPayload().get("shippingAddress"));
    }

    @Test
    void savePaymentStarted_shouldSavePaymentPayload() {
        BigDecimal totalAmount = new BigDecimal("59.98");
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.savePaymentStarted(order, totalAmount);

        assertEquals(OrderEventStatus.PAYMENT_STARTED, result.getStatus());
        assertEquals("STARTED", result.getPayload().get("paymentStatus"));
        assertEquals(totalAmount, result.getPayload().get("paymentRequestTotalAmount"));
    }

    @Test
    void saveRestored_shouldSaveRestoreTimestampPayload() {
        LocalDateTime restoreDate = LocalDateTime.now().minusMinutes(5);
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveRestored(order, restoreDate);

        assertEquals(OrderEventStatus.RESTORED, result.getStatus());
        assertEquals(restoreDate.toString(), result.getPayload().get("restoreTimestamp"));
    }

    @Test
    void saveDiscountChanged_shouldSaveDiscountPayload() {
        BigDecimal discountPercent = new BigDecimal("15.50");
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveDiscountChanged(
                order,
                discountPercent,
                OrderEventStatus.DISCOUNT_APPLIED
        );

        assertEquals(OrderEventStatus.DISCOUNT_APPLIED, result.getStatus());
        assertEquals(discountPercent, result.getPayload().get("discountPercent"));
    }

    @Test
    void savePaymentResult_shouldSavePaymentResultPayload() {
        LocalDateTime createdAt = LocalDateTime.now();
        PaymentCreatedEvent event = new PaymentCreatedEvent(
                "payment-1",
                1L,
                10L,
                "COMPLETED",
                new BigDecimal("59.98"),
                createdAt
        );
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.savePaymentResult(order, event, OrderEventStatus.PAID_SUCCESS);

        assertEquals(OrderEventStatus.PAID_SUCCESS, result.getStatus());
        assertEquals("payment-1", result.getPayload().get("paymentId"));
        assertEquals("COMPLETED", result.getPayload().get("paymentStatus"));
        assertEquals(new BigDecimal("59.98"), result.getPayload().get("paymentAmount"));
        assertEquals(createdAt.toString(), result.getPayload().get("paymentCreatedAt"));
    }

    @Test
    void saveConfirmationResult_shouldSaveConfirmationPayload() {
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveConfirmationResult(order, OrderEventStatus.CONFIRMED);

        assertEquals(OrderEventStatus.CONFIRMED, result.getStatus());
        assertEquals("CONFIRMED", result.getPayload().get("confirmationResult"));
    }

    @Test
    void saveDeliveryResult_shouldSaveDeliveryPayload() {
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveDeliveryResult(order, OrderEventStatus.DELIVERED, 42);

        assertEquals(OrderEventStatus.DELIVERED, result.getStatus());
        assertEquals("DELIVERED", result.getPayload().get("deliveryResult"));
        assertEquals(42, result.getPayload().get("deliveryRandomNumber"));
    }

    @Test
    void saveCreated_shouldAllowEmptyPayloadWhenAddressIsNull() {
        order.setShippingAddress(null);
        when(orderEventRepository.save(any(OrderEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEvent result = orderEventService.saveCreated(order);

        assertEquals(OrderEventStatus.CREATED, result.getStatus());
        assertNull(result.getPayload());
    }

    @Test
    void getOrderHistory_shouldDelegateToRepository() {
        when(orderEventRepository.findAllByOrderIdOrderByEventTimestampAsc(1L)).thenReturn(List.of(new OrderEvent()));

        List<OrderEvent> result = orderEventService.getOrderHistory(1L);

        assertEquals(1, result.size());
        verify(orderEventRepository).findAllByOrderIdOrderByEventTimestampAsc(1L);
    }

    @Test
    void getOrderHistoryUntil_shouldDelegateToRepository() {
        LocalDateTime date = LocalDateTime.now().minusMinutes(1);
        when(orderEventRepository.findAllByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampAsc(1L, date))
                .thenReturn(List.of(new OrderEvent()));

        List<OrderEvent> result = orderEventService.getOrderHistoryUntil(1L, date);

        assertEquals(1, result.size());
        verify(orderEventRepository).findAllByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampAsc(1L, date);
    }
}
