package com.internship.order_service.kafka;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedConsumer {

    private static final String ORDER_NOT_FOUND = "Order not found: ";

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-created", groupId = "order-service")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        try {
            log.info("Processing payment created event for order: {}", event.orderId());

            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND + event.orderId()));

            OrderStatus newOrderStatus = determineOrderStatus(event.status());
            order.setStatus(newOrderStatus);

            Order updatedOrder = orderRepository.save(order);

            log.info("Order status updated to {} for order: {}", newOrderStatus, event.orderId());

        } catch (Exception e) {
            log.error("Failed to process payment created event for order: {}", event.orderId(), e);
        }
    }

    private OrderStatus determineOrderStatus(String paymentStatus) {
        return switch (paymentStatus) {
            case "COMPLETED" -> OrderStatus.CONFIRMED;
            case "FAILED" -> OrderStatus.FAILED;
            case "CANCELLED" -> OrderStatus.CANCELLED;
            default -> OrderStatus.PENDING;
        };
    }
}