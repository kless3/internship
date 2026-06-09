package com.internship.order_service.messaging.sqs;

import com.internship.order_service.config.property.SqsProperties;
import com.internship.order_service.dto.event.OrderCreatedEvent;
import com.internship.order_service.messaging.OrderCreatedEventPublisher;
import com.internship.order_service.model.OrderEvent;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
@Slf4j
public class SqsOrderCreatedEventPublisher implements OrderCreatedEventPublisher {

    private static final String ORDER_CREATED_EVENT_PREFIX = "order-created-";

    private final SqsTemplate sqsTemplate;
    private final SqsProperties sqsProperties;

    @Override
    public void sendOrderCreatedEvent(OrderEvent orderEvent, BigDecimal totalAmount) {
        try {
            OrderCreatedEvent event = createOrderCreatedEvent(orderEvent, totalAmount);
            sendMessage(orderEvent, event);
        } catch (Exception e) {
            log.error("Error sending order created SQS event for order: {}", orderEvent.getId(), e);
        }
    }

    private OrderCreatedEvent createOrderCreatedEvent(OrderEvent orderEvent, BigDecimal totalAmount) {
        return new OrderCreatedEvent(
                orderEvent.getOrderId(),
                orderEvent.getUserId(),
                orderEvent.getUserEmail(),
                totalAmount,
                LocalDateTime.now()
        );
    }

    private void sendMessage(OrderEvent orderEvent, OrderCreatedEvent event) {
        sqsTemplate.send(to -> to
                .queue(sqsProperties.getOrderCreatedQueueName())
                .payload(event)
                .messageGroupId(String.valueOf(orderEvent.getOrderId()))
                .messageDeduplicationId(createDeduplicationId(orderEvent))
        );
    }

    private String createDeduplicationId(OrderEvent orderEvent) {
        if (orderEvent.getId() != null) {
            return ORDER_CREATED_EVENT_PREFIX + orderEvent.getId();
        }

        return ORDER_CREATED_EVENT_PREFIX + UUID.randomUUID();
    }
}
