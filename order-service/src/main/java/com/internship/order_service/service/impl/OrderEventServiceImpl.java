package com.internship.order_service.service.impl;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.payload.OrderEventPayload;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.service.OrderEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderEventServiceImpl implements OrderEventService {

    private final OrderEventRepository orderEventRepository;

    @Override
    public OrderEvent saveCreated(Order order) {
        return saveOrderEvent(
                order,
                OrderEventStatus.CREATED,
                OrderEventPayload.addressSnapshot(order.getShippingAddress())
        );
    }

    @Override
    public OrderEvent saveShippingAddressUpdated(Order order) {
        return saveOrderEvent(
                order,
                OrderEventStatus.SHIPPING_ADDRESS_UPDATED,
                OrderEventPayload.addressSnapshot(order.getShippingAddress())
        );
    }

    @Override
    public OrderEvent savePaymentStarted(Order order, BigDecimal totalAmount) {
        return saveOrderEvent(
                order,
                OrderEventStatus.PAYMENT_STARTED,
                OrderEventPayload.paymentStarted(totalAmount)
        );
    }

    @Override
    public OrderEvent saveRestored(Order order, LocalDateTime date) {
        return saveOrderEvent(
                order,
                OrderEventStatus.RESTORED,
                OrderEventPayload.restoreTimestamp(date)
        );
    }

    @Override
    public OrderEvent saveDiscountChanged(Order order, BigDecimal discountPercent, OrderEventStatus status) {
        return saveOrderEvent(
                order,
                status,
                OrderEventPayload.discount(discountPercent)
        );
    }

    @Override
    public OrderEvent savePaymentResult(Order order, PaymentCreatedEvent event, OrderEventStatus status) {
        return saveOrderEvent(
                order,
                status,
                OrderEventPayload.paymentCreated(
                        event.paymentId(),
                        event.status(),
                        event.amount(),
                        event.createdAt()
                )
        );
    }

    @Override
    public OrderEvent saveConfirmationResult(Order order, OrderEventStatus result) {
        return saveOrderEvent(
                order,
                result,
                OrderEventPayload.confirmationResult(result)
        );
    }

    @Override
    public OrderEvent saveDeliveryResult(Order order, OrderEventStatus result, Integer randomNumber) {
        return saveOrderEvent(
                order,
                result,
                OrderEventPayload.deliveryResult(result, randomNumber)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderEvent> getOrderHistory(Long orderId) {
        return orderEventRepository.findAllByOrderIdOrderByEventTimestampAsc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderEvent> getOrderHistoryUntil(Long orderId, LocalDateTime date) {
        return orderEventRepository.findAllByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampAsc(orderId, date);
    }

    private OrderEvent saveOrderEvent(Order order, OrderEventStatus status, Map<String, Object> payload) {
        OrderEvent orderEvent = createOrderEvent(order, status);
        orderEvent.setPayload(payload);
        return orderEventRepository.save(orderEvent);
    }

    private OrderEvent createOrderEvent(Order order, OrderEventStatus status) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(order.getId());
        orderEvent.setUserId(order.getUserId());
        orderEvent.setUserEmail(order.getUserEmail());
        orderEvent.setStatus(status);
        return orderEvent;
    }
}
