package com.internship.payment_service.messaging;

import com.internship.payment_service.model.Payment;

public interface PaymentCreatedEventPublisher {

    void sendPaymentCreatedEvent(Payment payment);
}
