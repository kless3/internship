package com.internship.order_service.messaging.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.order_service.config.property.SqsProperties;
import com.internship.order_service.dto.event.OrderCreatedEvent;
import com.internship.order_service.messaging.OrderCreatedEventPublisher;
import com.internship.order_service.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
@Slf4j
public class SqsOrderCreatedEventPublisher implements OrderCreatedEventPublisher {

    private static final String ORDER_CREATED_EVENT_PREFIX = "order-created-";

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;

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

            String queueUrl = getQueueUrl(sqsProperties.getOrderCreatedQueueName());
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId(String.valueOf(orderEvent.getOrderId()))
                    .messageDeduplicationId(createDeduplicationId(orderEvent))
                    .build();

            sqsClient.sendMessage(request);
        } catch (Exception e) {
            log.error("Error sending order created SQS event for order: {}", orderEvent.getId(), e);
        }
    }

    private String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    }

    private String createDeduplicationId(OrderEvent orderEvent) {
        if (orderEvent.getId() != null) {
            return ORDER_CREATED_EVENT_PREFIX + orderEvent.getId();
        }

        return ORDER_CREATED_EVENT_PREFIX + UUID.randomUUID();
    }
}
