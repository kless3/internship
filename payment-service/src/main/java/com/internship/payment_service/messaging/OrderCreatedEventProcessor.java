package com.internship.payment_service.messaging;

import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.dto.event.OrderCreatedEvent;
import com.internship.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventProcessor {

    private final PaymentService paymentService;

    public void process(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, userId={}, amount={}",
                event.orderId(), event.userId(), event.totalAmount());

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO(event.orderId(), event.userId(), event.totalAmount());

        PaymentResponseDTO response = paymentService.createPayment(paymentRequest);
        log.info("Payment created successfully: {}", response.id());
    }
}
