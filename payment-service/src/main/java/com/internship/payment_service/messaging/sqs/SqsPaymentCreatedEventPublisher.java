package com.internship.payment_service.messaging.sqs;

import com.internship.payment_service.config.property.SqsProperties;
import com.internship.payment_service.dto.event.PaymentCreatedEvent;
import com.internship.payment_service.messaging.PaymentCreatedEventPublisher;
import com.internship.payment_service.model.Payment;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
@Slf4j
public class SqsPaymentCreatedEventPublisher implements PaymentCreatedEventPublisher {

    private static final String PAYMENT_CREATED_EVENT_PREFIX = "payment-created-";

    private final SqsTemplate sqsTemplate;
    private final SqsProperties sqsProperties;

    @Override
    public void sendPaymentCreatedEvent(Payment payment) {
        try {
            PaymentCreatedEvent event = createPaymentCreatedEvent(payment);
            sendMessage(payment, event);
        } catch (Exception e) {
            log.error("Error sending payment SQS event for order: {}", payment.getOrderId(), e);
        }
    }

    private PaymentCreatedEvent createPaymentCreatedEvent(Payment payment) {
        return new PaymentCreatedEvent(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getStatus().toString(),
                payment.getPaymentAmount(),
                LocalDateTime.now()
        );
    }

    private void sendMessage(Payment payment, PaymentCreatedEvent event) {
        sqsTemplate.send(to -> to
                .queue(sqsProperties.getPaymentCreatedQueueName())
                .payload(event)
                .messageGroupId(String.valueOf(payment.getOrderId()))
                .messageDeduplicationId(createDeduplicationId(payment))
        );
    }

    private String createDeduplicationId(Payment payment) {
        if (payment.getId() != null) {
            return PAYMENT_CREATED_EVENT_PREFIX + payment.getId();
        }

        return PAYMENT_CREATED_EVENT_PREFIX + UUID.randomUUID();
    }
}
