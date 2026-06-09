package com.internship.order_service.messaging.sqs;

import com.internship.order_service.dto.event.PaymentCreatedEvent;
import com.internship.order_service.messaging.PaymentCreatedEventProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
public class PaymentCreatedSqsConsumer {

    private final PaymentCreatedEventProcessor paymentCreatedEventProcessor;

    @SqsListener("${spring.sqs.payment-created-queue-name}")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        paymentCreatedEventProcessor.process(event);
    }
}
