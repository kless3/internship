package com.internship.payment_service.service.impl;

import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.exception.InvalidPaymentStatusException;
import com.internship.payment_service.exception.PaymentNotFoundException;
import com.internship.payment_service.kafka.PaymentCreatedProducer;
import com.internship.payment_service.mapper.PaymentMapper;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.model.enums.PaymentStatus;
import com.internship.payment_service.repository.PaymentRepository;
import com.internship.payment_service.rest.ExternalApiClient;
import com.internship.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_NOT_FOUND = "Payment not found with id: ";
    private static final String NO_PAYMENTS_FOR_USER = "No payments found for user id: ";
    private static final String NO_PAYMENTS_FOR_ORDER = "No payments found for order id: ";
    private static final String NO_PAYMENTS_FOR_STATUS = "No payments found with status: ";
    private static final String INVALID_PAYMENT_STATUS = "Payment status cannot be null";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ExternalApiClient externalApiClient;
    private final PaymentCreatedProducer paymentCreatedProducer;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {
        Payment payment = paymentMapper.toEntity(requestDTO);
        PaymentStatus status = determinePaymentStatus();
        payment.setStatus(status);

        Payment savedPayment = paymentRepository.save(payment);
        paymentCreatedProducer.sendPaymentCreatedEvent(savedPayment);
        return paymentMapper.toResponseDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(PAYMENT_NOT_FOUND + id));
        return paymentMapper.toResponseDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByUserId(Long userId) {

        List<Payment> payments = paymentRepository.findByUserId(userId);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException(NO_PAYMENTS_FOR_USER + userId);
        }
        return payments.stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId) {

        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException(NO_PAYMENTS_FOR_ORDER + orderId);
        }
        return payments.stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new InvalidPaymentStatusException(INVALID_PAYMENT_STATUS);
        }

        List<Payment> payments = paymentRepository.findByStatus(paymentStatus);
        if (payments.isEmpty()) {
            throw new PaymentNotFoundException(NO_PAYMENTS_FOR_STATUS + paymentStatus);
        }
        return payments.stream()
                .map(paymentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSumOfPaymentsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {

        List<Payment> completedPayments = paymentRepository.findCompletedPaymentsInDateRange(startDate, endDate);
        return completedPayments.stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentStatus determinePaymentStatus() {
        boolean isCompleted = externalApiClient.getRandomNumber() % 2 == 0;
        return isCompleted ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;
    }

}