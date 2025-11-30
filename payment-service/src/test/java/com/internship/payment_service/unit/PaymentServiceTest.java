package com.internship.payment_service.unit;

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
import com.internship.payment_service.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ExternalApiClient externalApiClient;

    @Mock
    private PaymentCreatedProducer paymentCreatedProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should create payment successfully when request is valid")
    void createPayment_ValidRequest_ReturnsPaymentResponse() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(1L, 1L, new BigDecimal("100.00"));
        Payment payment = new Payment();
        Payment savedPayment = new Payment();
        PaymentResponseDTO expectedResponse = new PaymentResponseDTO("1", 1L, 1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());

        when(paymentMapper.toEntity(requestDTO)).thenReturn(payment);
        when(externalApiClient.getRandomNumber()).thenReturn(2);
        when(paymentRepository.save(payment)).thenReturn(savedPayment);
        when(paymentMapper.toResponseDTO(savedPayment)).thenReturn(expectedResponse);

        PaymentResponseDTO actualResponse = paymentService.createPayment(requestDTO);

        assertEquals(expectedResponse, actualResponse);
        verify(paymentCreatedProducer).sendPaymentCreatedEvent(savedPayment);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Should return payment when payment exists with given id")
    void getPaymentById_ExistingId_ReturnsPaymentResponse() {
        String paymentId = "123";
        Payment payment = new Payment();
        PaymentResponseDTO expectedResponse = new PaymentResponseDTO("123", 1L, 1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponseDTO(payment)).thenReturn(expectedResponse);

        PaymentResponseDTO actualResponse = paymentService.getPaymentById(paymentId);

        assertEquals(expectedResponse, actualResponse);
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment not found with given id")
    void getPaymentById_NonExistingId_ThrowsException() {
        String paymentId = "999";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(paymentId));
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    @DisplayName("Should return all payments when payments exist")
    void getAllPayments_PaymentsExist_ReturnsPaymentList() {
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        List<Payment> payments = Arrays.asList(payment1, payment2);
        PaymentResponseDTO response1 = new PaymentResponseDTO("1", 1L, 1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());
        PaymentResponseDTO response2 = new PaymentResponseDTO("2", 2L, 2L, new BigDecimal("200.00"), PaymentStatus.FAILED, LocalDateTime.now());

        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentMapper.toResponseDTO(payment1)).thenReturn(response1);
        when(paymentMapper.toResponseDTO(payment2)).thenReturn(response2);

        List<PaymentResponseDTO> actualResponses = paymentService.getAllPayments();

        assertEquals(2, actualResponses.size());
        assertTrue(actualResponses.contains(response1));
        assertTrue(actualResponses.contains(response2));
        verify(paymentRepository).findAll();
    }

    @Test
    @DisplayName("Should return payments for user when user has payments")
    void getPaymentsByUserId_UserHasPayments_ReturnsPaymentList() {
        Long userId = 1L;
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        List<Payment> payments = Arrays.asList(payment1, payment2);
        PaymentResponseDTO response1 = new PaymentResponseDTO("1", userId, 1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());
        PaymentResponseDTO response2 = new PaymentResponseDTO("2", userId, 2L, new BigDecimal("200.00"), PaymentStatus.COMPLETED, LocalDateTime.now());

        when(paymentRepository.findByUserId(userId)).thenReturn(payments);
        when(paymentMapper.toResponseDTO(payment1)).thenReturn(response1);
        when(paymentMapper.toResponseDTO(payment2)).thenReturn(response2);

        List<PaymentResponseDTO> actualResponses = paymentService.getPaymentsByUserId(userId);

        assertEquals(2, actualResponses.size());
        assertTrue(actualResponses.contains(response1));
        assertTrue(actualResponses.contains(response2));
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when user has no payments")
    void getPaymentsByUserId_UserNoPayments_ThrowsException() {
        Long userId = 999L;

        when(paymentRepository.findByUserId(userId)).thenReturn(List.of());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentsByUserId(userId));
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return payments for order when order has payments")
    void getPaymentsByOrderId_OrderHasPayments_ReturnsPaymentList() {
        Long orderId = 1L;
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        List<Payment> payments = Arrays.asList(payment1, payment2);
        PaymentResponseDTO response1 = new PaymentResponseDTO("1", 1L, orderId, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());
        PaymentResponseDTO response2 = new PaymentResponseDTO("2", 2L, orderId, new BigDecimal("200.00"), PaymentStatus.COMPLETED, LocalDateTime.now());

        when(paymentRepository.findByOrderId(orderId)).thenReturn(payments);
        when(paymentMapper.toResponseDTO(payment1)).thenReturn(response1);
        when(paymentMapper.toResponseDTO(payment2)).thenReturn(response2);

        List<PaymentResponseDTO> actualResponses = paymentService.getPaymentsByOrderId(orderId);

        assertEquals(2, actualResponses.size());
        assertTrue(actualResponses.contains(response1));
        assertTrue(actualResponses.contains(response2));
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when order has no payments")
    void getPaymentsByOrderId_OrderNoPayments_ThrowsException() {
        Long orderId = 999L;

        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentsByOrderId(orderId));
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("Should return payments when payments exist with given status")
    void getPaymentsByStatus_ValidStatus_ReturnsPaymentList() {
        PaymentStatus status = PaymentStatus.COMPLETED;
        Payment payment1 = new Payment();
        Payment payment2 = new Payment();
        List<Payment> payments = Arrays.asList(payment1, payment2);
        PaymentResponseDTO response1 = new PaymentResponseDTO("1", 1L, 1L, new BigDecimal("100.00"), status, LocalDateTime.now());
        PaymentResponseDTO response2 = new PaymentResponseDTO("2", 2L, 2L, new BigDecimal("200.00"), status, LocalDateTime.now());

        when(paymentRepository.findByStatus(status)).thenReturn(payments);
        when(paymentMapper.toResponseDTO(payment1)).thenReturn(response1);
        when(paymentMapper.toResponseDTO(payment2)).thenReturn(response2);

        List<PaymentResponseDTO> actualResponses = paymentService.getPaymentsByStatus(status);

        assertEquals(2, actualResponses.size());
        assertTrue(actualResponses.contains(response1));
        assertTrue(actualResponses.contains(response2));
        verify(paymentRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should throw InvalidPaymentStatusException when payment status is null")
    void getPaymentsByStatus_NullStatus_ThrowsException() {
        assertThrows(InvalidPaymentStatusException.class, () -> paymentService.getPaymentsByStatus(null));
        verify(paymentRepository, never()).findByStatus(any());
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when no payments found with given status")
    void getPaymentsByStatus_NoPaymentsWithStatus_ThrowsException() {
        PaymentStatus status = PaymentStatus.FAILED;

        when(paymentRepository.findByStatus(status)).thenReturn(List.of());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentsByStatus(status));
        verify(paymentRepository).findByStatus(status);
    }

    @Test
    @DisplayName("Should return total sum of completed payments for given period")
    void getTotalSumOfPaymentsForPeriod_ValidPeriod_ReturnsTotalSum() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        Payment payment1 = new Payment();
        payment1.setPaymentAmount(new BigDecimal("100.50"));
        Payment payment2 = new Payment();
        payment2.setPaymentAmount(new BigDecimal("200.75"));
        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(paymentRepository.findCompletedPaymentsInDateRange(startDate, endDate)).thenReturn(payments);

        BigDecimal totalSum = paymentService.getTotalSumOfPaymentsForPeriod(startDate, endDate);

        assertEquals(new BigDecimal("301.25"), totalSum);
        verify(paymentRepository).findCompletedPaymentsInDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should return zero when no completed payments in given period")
    void getTotalSumOfPaymentsForPeriod_NoPayments_ReturnsZero() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(paymentRepository.findCompletedPaymentsInDateRange(startDate, endDate)).thenReturn(List.of());

        BigDecimal totalSum = paymentService.getTotalSumOfPaymentsForPeriod(startDate, endDate);

        assertEquals(BigDecimal.ZERO, totalSum);
        verify(paymentRepository).findCompletedPaymentsInDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("Should set completed status when external API returns even number")
    void determinePaymentStatus_EvenNumber_ReturnsCompleted() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(1L, 1L, new BigDecimal("100.00"));
        Payment payment = new Payment();
        Payment savedPayment = new Payment();
        PaymentResponseDTO expectedResponse = new PaymentResponseDTO("1", 1L, 1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED, LocalDateTime.now());

        when(paymentMapper.toEntity(requestDTO)).thenReturn(payment);
        when(externalApiClient.getRandomNumber()).thenReturn(4);
        when(paymentRepository.save(payment)).thenReturn(savedPayment);
        when(paymentMapper.toResponseDTO(savedPayment)).thenReturn(expectedResponse);

        PaymentResponseDTO result = paymentService.createPayment(requestDTO);

        assertEquals(PaymentStatus.COMPLETED, result.status());
    }

    @Test
    @DisplayName("Should set failed status when external API returns odd number")
    void determinePaymentStatus_OddNumber_ReturnsFailed() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(1L, 1L, new BigDecimal("100.00"));
        Payment payment = new Payment();
        Payment savedPayment = new Payment();
        PaymentResponseDTO expectedResponse = new PaymentResponseDTO("1", 1L, 1L, new BigDecimal("100.00"), PaymentStatus.FAILED, LocalDateTime.now());

        when(paymentMapper.toEntity(requestDTO)).thenReturn(payment);
        when(externalApiClient.getRandomNumber()).thenReturn(3);
        when(paymentRepository.save(payment)).thenReturn(savedPayment);
        when(paymentMapper.toResponseDTO(savedPayment)).thenReturn(expectedResponse);

        PaymentResponseDTO result = paymentService.createPayment(requestDTO);

        assertEquals(PaymentStatus.FAILED, result.status());
    }
}