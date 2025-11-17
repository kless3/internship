package com.internship.payment_service.service;

import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO);

    PaymentResponseDTO getPaymentById(String id);

    List<PaymentResponseDTO> getAllPayments();

    List<PaymentResponseDTO> getPaymentsByUserId(Long userId);

    List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId);

    List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus paymentStatus);

    BigDecimal getTotalSumOfPaymentsForPeriod(LocalDateTime startDate, LocalDateTime endDate);

}
