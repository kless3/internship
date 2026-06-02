package com.internship.order_service.service;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderEventService {

    OrderEvent saveCreated(Order order);

    OrderEvent saveShippingAddressUpdated(Order order);

    OrderEvent savePaymentStarted(Order order, BigDecimal totalAmount);

    OrderEvent saveRestored(Order order, LocalDateTime date);

    OrderEvent saveDiscountChanged(Order order, BigDecimal discountPercent, OrderEventStatus status);

    OrderEvent savePaymentResult(Order order, PaymentCreatedEvent event, OrderEventStatus status);

    OrderEvent saveConfirmationResult(Order order, OrderEventStatus result);

    OrderEvent saveDeliveryResult(Order order, OrderEventStatus result, Integer randomNumber);

    List<OrderEvent> getOrderHistory(Long orderId);

    List<OrderEvent> getOrderHistoryUntil(Long orderId, LocalDateTime date);
}
