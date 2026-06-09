package com.internship.order_service.messaging.kafka;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.messaging.PaymentCreatedEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "kafka", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedConsumer {

    private final PaymentCreatedEventProcessor paymentCreatedEventProcessor;

    @KafkaListener(topics = "payment-created", groupId = "order-service")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        try {
            paymentCreatedEventProcessor.process(event);
        } catch (Exception e) {
            log.error("Failed to process payment created event for order: {}", event.orderId(), e);
        }
    }
}
