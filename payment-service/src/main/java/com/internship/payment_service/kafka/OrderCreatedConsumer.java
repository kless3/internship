package com.internship.payment_service.kafka;

import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.dto.event.OrderCreatedEvent;
import com.internship.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-created", groupId = "payment-service", containerFactory = "orderKafkaListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, userId={}, amount={}",
                    event.orderId(), event.userId(), event.totalAmount());

            PaymentRequestDTO paymentRequest = new PaymentRequestDTO(event.orderId(), event.userId(), event.totalAmount());

            PaymentResponseDTO response = paymentService.createPayment(paymentRequest);
            log.info("Payment created successfully: {}", response.id());

        } catch (Exception e) {
            log.error("Failed to process order created event: {}", e.getMessage(), e);
        }
    }
}