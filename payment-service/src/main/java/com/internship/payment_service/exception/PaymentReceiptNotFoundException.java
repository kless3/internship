package com.internship.payment_service.exception;

public class PaymentReceiptNotFoundException extends RuntimeException {

    public PaymentReceiptNotFoundException(String message) {
        super(message);
    }
}
