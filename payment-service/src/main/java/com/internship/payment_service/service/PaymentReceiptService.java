package com.internship.payment_service.service;

import com.internship.payment_service.model.Payment;

public interface PaymentReceiptService {

    String createReceipt(Payment payment);

    byte[] getReceipt(String receiptKey);
}
