package com.internship.order_service.unit;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderEventService;
import com.internship.order_service.service.impl.DeliverySimulatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverySimulatorServiceTest {

    @Mock
    private OrderEventService orderEventService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private DeliverySimulatorServiceImpl deliverySimulatorService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setUserEmail("test@example.com");
        order.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void simulateConfirmation_shouldSetFailedWhenPaymentFailed() {
        deliverySimulatorService.simulateConfirmation(order, OrderEventStatus.PAID_FAILED);

        assertEquals(OrderStatus.FAILED, order.getStatus());
        verify(orderEventService).saveConfirmationResult(order, OrderEventStatus.REJECTED);
        verify(orderEventService, never()).saveDeliveryResult(any(Order.class), any(OrderEventStatus.class), anyInt());
    }

    @Test
    void simulateConfirmation_shouldSetConfirmedAndTriggerDeliveryWhenPaymentSuccess() {
        deliverySimulatorService.simulateConfirmation(order, OrderEventStatus.PAID_SUCCESS);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(orderEventService).saveConfirmationResult(order, OrderEventStatus.CONFIRMED);
        verify(orderEventService).saveDeliveryResult(any(Order.class), any(OrderEventStatus.class), anyInt());
    }

    @Test
    void simulateDelivery_shouldSetTerminalStatusAndSaveEvent() {
        deliverySimulatorService.simulateDelivery(order);

        boolean validTerminal = order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REFUNDED;
        assertTrue(validTerminal);

        verify(orderRepository).save(any(Order.class));
        verify(orderEventService).saveDeliveryResult(any(Order.class), any(OrderEventStatus.class), anyInt());
    }
}
