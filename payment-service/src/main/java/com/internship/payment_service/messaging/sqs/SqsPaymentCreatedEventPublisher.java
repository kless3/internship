package com.internship.payment_service.messaging.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.payment_service.config.property.SqsProperties;
import com.internship.payment_service.dto.event.PaymentCreatedEvent;
import com.internship.payment_service.messaging.PaymentCreatedEventPublisher;
import com.internship.payment_service.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
@Slf4j
public class SqsPaymentCreatedEventPublisher implements PaymentCreatedEventPublisher {

    private static final String PAYMENT_CREATED_EVENT_PREFIX = "payment-created-";

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

    @Override
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

            String queueUrl = getQueueUrl(sqsProperties.getPaymentCreatedQueueName());
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId(String.valueOf(payment.getOrderId()))
                    .messageDeduplicationId(createDeduplicationId(payment))
                    .build();

            sqsClient.sendMessage(request);
        } catch (Exception e) {
            log.error("Error sending payment SQS event for order: {}", payment.getOrderId(), e);
        }
    }

    private String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    }

    private String createDeduplicationId(Payment payment) {
        if (payment.getId() != null) {
            return PAYMENT_CREATED_EVENT_PREFIX + payment.getId();
        }

        return PAYMENT_CREATED_EVENT_PREFIX + UUID.randomUUID();
    }
}
