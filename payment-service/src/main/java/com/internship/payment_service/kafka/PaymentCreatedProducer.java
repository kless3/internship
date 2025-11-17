package com.internship.payment_service.kafka;

import com.internship.payment_service.dto.event.PaymentCreatedEvent;
import com.internship.payment_service.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedProducer {

    private static final String PAYMENT_CREATED_TOPIC = "payment-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCreatedEvent(Payment payment) {
        try {
            PaymentCreatedEvent event = new PaymentCreatedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getStatus().toString(),
                    payment.getPaymentAmount(),
                    LocalDateTime.now()
            );

            kafkaTemplate.send(PAYMENT_CREATED_TOPIC, payment.getOrderId().toString(), event);

        } catch (Exception e) {
            log.error("Error sending payment event for order: {}", payment.getOrderId(), e);
        }
    }
}