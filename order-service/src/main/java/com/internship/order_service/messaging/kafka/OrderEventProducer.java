package com.internship.order_service.messaging.kafka;

import com.internship.order_service.dto.event.OrderCreatedEvent;
import com.internship.order_service.messaging.OrderCreatedEventPublisher;
import com.internship.order_service.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "kafka", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer implements OrderCreatedEventPublisher {

    private static final String ORDER_CREATED_TOPIC = "order-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendOrderCreatedEvent(OrderEvent orderEvent, BigDecimal totalAmount) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    orderEvent.getOrderId(),
                    orderEvent.getUserId(),
                    orderEvent.getUserEmail(),
                    totalAmount,
                    LocalDateTime.now()
            );

            kafkaTemplate.send(ORDER_CREATED_TOPIC, String.valueOf(orderEvent.getOrderId()), event);
        } catch (Exception e) {
            log.error("Error sending order created event for order: {}", orderEvent.getId(), e);
        }
    }
}
