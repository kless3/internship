package com.internship.payment_service.messaging.kafka;

import com.internship.payment_service.dto.event.OrderCreatedEvent;
import com.internship.payment_service.messaging.OrderCreatedEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "kafka", matchIfMissing = true)
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final OrderCreatedEventProcessor orderCreatedEventProcessor;

    @KafkaListener(topics = "order-created", groupId = "payment-service", containerFactory = "orderKafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        orderCreatedEventProcessor.process(event);
    }
}
