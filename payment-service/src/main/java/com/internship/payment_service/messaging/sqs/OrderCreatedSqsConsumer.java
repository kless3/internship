package com.internship.payment_service.messaging.sqs;

import com.internship.payment_service.dto.event.OrderCreatedEvent;
import com.internship.payment_service.messaging.OrderCreatedEventProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
public class OrderCreatedSqsConsumer {

    private final OrderCreatedEventProcessor orderCreatedEventProcessor;

    @SqsListener("${spring.sqs.order-created-queue-name}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        orderCreatedEventProcessor.process(event);
    }
}
