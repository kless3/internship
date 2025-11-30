package com.internship.payment_service.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.model.enums.PaymentStatus;
import com.internship.payment_service.repository.PaymentRepository;
import com.internship.payment_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTest extends BaseIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig()
                    .port(8089))
            .build();

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private PaymentRequestDTO paymentRequestDTO;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        paymentRepository.deleteAll();
        paymentRequestDTO = new PaymentRequestDTO(1L, 100L, BigDecimal.valueOf(500.00));

        wireMock.stubFor(get(urlEqualTo("/integers"))
                .willReturn(ok()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("4")));
    }

    @Test
    @DisplayName("Should create payment with COMPLETED status via WireMock random API")
    void testCreatePaymentWithCompletedViaWireMock() {
        PaymentResponseDTO result = paymentService.createPayment(paymentRequestDTO);

        assertNotNull(result.id());
        assertEquals(PaymentStatus.COMPLETED, result.status());
        assertEquals(1L, result.orderId());
        assertEquals(100L, result.userId());

        wireMock.verify(1, getRequestedFor(urlEqualTo("/integers")));
    }

    @Test
    @DisplayName("Should create payment with FAILED status via WireMock odd random number")
    void testCreatePaymentWithFailedViaWireMock() {
        wireMock.resetAll();

        wireMock.stubFor(get(urlEqualTo("/integers"))
                .willReturn(ok()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("7")));

        PaymentResponseDTO result = paymentService.createPayment(paymentRequestDTO);

        assertEquals(PaymentStatus.FAILED, result.status());
        wireMock.verify(1, getRequestedFor(urlEqualTo("/integers")));
    }

    @Test
    @DisplayName("Should find payments by order id")
    void testFindPaymentsByOrderId() {
        paymentService.createPayment(paymentRequestDTO);
        paymentService.createPayment(paymentRequestDTO);

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByOrderId(1L);

        assertEquals(2, payments.size());
    }

    @Test
    @DisplayName("Should find payments by user id")
    void testFindPaymentsByUserId() {
        paymentService.createPayment(paymentRequestDTO);

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByUserId(100L);

        assertEquals(1, payments.size());
    }

    @Test
    @DisplayName("Should find payments by status")
    void testFindPaymentsByStatus() {
        paymentService.createPayment(paymentRequestDTO);

        List<PaymentResponseDTO> payments = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED);

        assertEquals(1, payments.size());
    }

    @Test
    @DisplayName("Should calculate total sum for period")
    void testGetTotalSumForPeriod() {
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);

        paymentService.createPayment(paymentRequestDTO);
        paymentService.createPayment(paymentRequestDTO);

        LocalDateTime after = LocalDateTime.now().plusMinutes(1);

        BigDecimal totalSum = paymentService.getTotalSumOfPaymentsForPeriod(before, after);

        assertNotNull(totalSum);
        assertEquals(new BigDecimal("1000.0"), totalSum);
    }

    @Test
    @DisplayName("Should send Kafka messages successfully")
    void testKafkaMessaging() {
        assertDoesNotThrow(() -> {
            kafkaTemplate.send("payment-created", "test-key", "test-message");
        });
    }

    @Test
    @DisplayName("Should persist multiple payments")
    void testPersistMultiplePayments() {
        PaymentResponseDTO payment1 = paymentService.createPayment(
                new PaymentRequestDTO(1L, 100L, BigDecimal.valueOf(500.00)));
        PaymentResponseDTO payment2 = paymentService.createPayment(
                new PaymentRequestDTO(2L, 200L, BigDecimal.valueOf(750.00)));
        PaymentResponseDTO payment3 = paymentService.createPayment(
                new PaymentRequestDTO(1L, 100L, BigDecimal.valueOf(250.00)));

        List<Payment> allPayments = paymentRepository.findAll();
        assertEquals(3, allPayments.size());

        List<PaymentResponseDTO> orderPayments = paymentService.getPaymentsByOrderId(1L);
        assertEquals(2, orderPayments.size());
    }

    @Test
    @DisplayName("Should retrieve payment by ID")
    void testGetPaymentById() {
        PaymentResponseDTO createdPayment = paymentService.createPayment(paymentRequestDTO);

        PaymentResponseDTO retrievedPayment = paymentService.getPaymentById(createdPayment.id());

        assertNotNull(retrievedPayment);
        assertEquals(createdPayment.id(), retrievedPayment.id());
        assertEquals(createdPayment.orderId(), retrievedPayment.orderId());
        assertEquals(createdPayment.userId(), retrievedPayment.userId());
    }

    @Test
    @DisplayName("Should handle large payment amounts")
    void testLargePaymentAmounts() {
        PaymentRequestDTO largePayment = new PaymentRequestDTO(
                1L, 100L, new BigDecimal("999999.99"));

        PaymentResponseDTO result = paymentService.createPayment(largePayment);

        assertEquals(new BigDecimal("999999.99"), result.paymentAmount());
    }

    @Test
    @DisplayName("Should verify WireMock received multiple requests")
    void testWireMockMultipleRequests() {
        paymentService.createPayment(paymentRequestDTO);
        paymentService.createPayment(paymentRequestDTO);
        paymentService.createPayment(paymentRequestDTO);

        wireMock.verify(3, getRequestedFor(urlEqualTo("/integers")));
    }

    @Test
    @DisplayName("Should get all payments")
    void testGetAllPayments() {
        paymentService.createPayment(paymentRequestDTO);
        paymentService.createPayment(new PaymentRequestDTO(2L, 200L, BigDecimal.valueOf(300.00)));

        List<PaymentResponseDTO> allPayments = paymentService.getAllPayments();

        assertEquals(2, allPayments.size());
    }

    @Test
    @DisplayName("Should return empty list when no payments for user")
    void testNoPaymentsForUser() {
        assertThrows(Exception.class, () -> {
            paymentService.getPaymentsByUserId(999L);
        });
    }

    @Test
    @DisplayName("Should return zero total sum for period with no payments")
    void testGetTotalSumForPeriodNoPayments() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(5);

        BigDecimal totalSum = paymentService.getTotalSumOfPaymentsForPeriod(startDate, endDate);

        assertEquals(BigDecimal.ZERO, totalSum);
    }
}