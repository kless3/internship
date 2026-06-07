package com.internship.order_service.messaging;

import com.internship.order_service.model.OrderEvent;

import java.math.BigDecimal;

public interface OrderCreatedEventPublisher {

    void sendOrderCreatedEvent(OrderEvent orderEvent, BigDecimal totalAmount);
}
