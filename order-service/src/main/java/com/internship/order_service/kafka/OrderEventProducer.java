package com.internship.order_service.kafka;

import com.internship.order_service.dto.event.OrderCreatedEvent;
import com.internship.order_service.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String ORDER_CREATED_TOPIC = "order-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(Order order) {
        try {
            BigDecimal totalAmount = order.getOrderItems().stream()
                    .map(orderItem ->
                            orderItem.getItem().getPrice()
                                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderCreatedEvent event = new OrderCreatedEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getUserEmail(),
                    totalAmount,
                    LocalDateTime.now()
            );

            kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(order.getId()), event);
        } catch (Exception e) {
            log.error("Error sending order created event for order: {}", order.getId(), e);
        }
    }
}