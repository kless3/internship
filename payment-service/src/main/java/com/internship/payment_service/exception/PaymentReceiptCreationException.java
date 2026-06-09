package com.internship.payment_service.exception;

public class PaymentReceiptCreationException extends RuntimeException {

    public PaymentReceiptCreationException(String message, Throwable cause) {
        super(message);
    }
}
