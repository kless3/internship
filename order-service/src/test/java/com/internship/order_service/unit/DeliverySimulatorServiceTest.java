package com.internship.order_service.unit;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.impl.DeliverySimulatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverySimulatorServiceTest {

    @Mock
    private OrderEventRepository orderEventRepository;

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
        verify(orderEventRepository).save(any(OrderEvent.class));
    }

    @Test
    void simulateConfirmation_shouldSetConfirmedAndTriggerDeliveryWhenPaymentSuccess() {
        deliverySimulatorService.simulateConfirmation(order, OrderEventStatus.PAID_SUCCESS);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(orderEventRepository, atLeastOnce()).save(any(OrderEvent.class));
    }

    @Test
    void simulateDelivery_shouldSetTerminalStatusAndSaveEvent() {
        deliverySimulatorService.simulateDelivery(order);

        boolean validTerminal = order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.REFUNDED;
        assertEquals(true, validTerminal);

        verify(orderRepository).save(any(Order.class));
        verify(orderEventRepository).save(any(OrderEvent.class));
    }
}
