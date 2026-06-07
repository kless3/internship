package com.internship.payment_service.messaging.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.payment_service.config.property.SqsProperties;
import com.internship.payment_service.dto.event.OrderCreatedEvent;
import com.internship.payment_service.messaging.OrderCreatedEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedSqsConsumer {

    private final SqsClient sqsClient;
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;
    private final OrderCreatedEventProcessor orderCreatedEventProcessor;

    @Scheduled(fixedDelayString = "${spring.sqs.poll-delay-ms:1000}")
    public void pollOrderCreatedQueue() {
        String queueUrl = getQueueUrl(sqsProperties.getOrderCreatedQueueName());

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(sqsProperties.getMaxMessages())
                .waitTimeSeconds(sqsProperties.getWaitTimeSeconds())
                .build();

        for (Message message : sqsClient.receiveMessage(request).messages()) {
            handleMessage(queueUrl, message);
        }
    }

    private void handleMessage(String queueUrl, Message message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message.body(), OrderCreatedEvent.class);
            orderCreatedEventProcessor.process(event);
            deleteMessage(queueUrl, message);
        } catch (Exception e) {
            log.error("Failed to process order created SQS message: {}", message.messageId(), e);
        }
    }

    private String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    }

    private void deleteMessage(String queueUrl, Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }
}
