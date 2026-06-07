package com.internship.payment_service.exception;

public class PaymentReceiptException extends RuntimeException {

    public PaymentReceiptException(String message, Throwable cause) {
        super(message, cause);
    }
}
