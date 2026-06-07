package com.internship.payment_service.unit;

import com.internship.payment_service.config.property.S3Properties;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.model.enums.PaymentStatus;
import com.internship.payment_service.service.impl.PaymentReceiptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Receipt Service Unit Tests")
class PaymentReceiptServiceTest {

    private static final String BUCKET_NAME = "payment-receipts";

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private S3Client s3Client;

    private PaymentReceiptServiceImpl paymentReceiptService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setReceiptBucketName(BUCKET_NAME);
        paymentReceiptService = new PaymentReceiptServiceImpl(templateEngine, s3Client, s3Properties);
    }

    @Test
    @DisplayName("Should generate receipt pdf and upload it to S3")
    void createReceipt_Payment_UploadsPdfAndReturnsReceiptKey() {
        Payment payment = createPayment();
        when(templateEngine.process(eq("receipt"), any(Context.class)))
                .thenReturn("<html><body><h1>Receipt</h1><p>Payment amount: 100.00 USD</p></body></html>");

        String receiptKey = paymentReceiptService.createReceipt(payment);

        assertEquals("receipts/order-10/payment-payment-1.pdf", receiptKey);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

        PutObjectRequest request = requestCaptor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(receiptKey, request.key());
        assertEquals("application/pdf", request.contentType());
        assertTrue(request.contentLength() > 0);
        assertTrue(bodyCaptor.getValue().contentLength() > 0);
    }

    @Test
    @DisplayName("Should include failed payment message in receipt context")
    void createReceipt_FailedPayment_UsesFailedReceiptText() {
        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.FAILED);
        when(templateEngine.process(eq("receipt"), any(Context.class)))
                .thenReturn("<html><body><h1>Payment failed</h1><p>Payment amount: 100.00 USD</p></body></html>");

        String receiptKey = paymentReceiptService.createReceipt(payment);

        assertEquals("receipts/order-10/payment-payment-1.pdf", receiptKey);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("receipt"), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertEquals(PaymentStatus.FAILED, context.getVariable("paymentStatus"));
        assertEquals("Payment failed", context.getVariable("statusMessage"));
        assertNull(context.getVariable("orderId"));
        assertNull(context.getVariable("userId"));

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should download receipt pdf from S3")
    void getReceipt_ExistingReceiptKey_ReturnsPdfBytes() {
        String receiptKey = "receipts/order-10/payment-payment-1.pdf";
        byte[] receipt = "%PDF".getBytes();
        ResponseBytes<GetObjectResponse> response = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().contentLength((long) receipt.length).build(),
                receipt
        );

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(response);

        byte[] actualReceipt = paymentReceiptService.getReceipt(receiptKey);

        assertArrayEquals(receipt, actualReceipt);

        ArgumentCaptor<GetObjectRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(requestCaptor.capture());

        GetObjectRequest request = requestCaptor.getValue();
        assertEquals(BUCKET_NAME, request.bucket());
        assertEquals(receiptKey, request.key());
    }

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setId("payment-1");
        payment.setOrderId(10L);
        payment.setUserId(20L);
        payment.setPaymentAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTimestamp(LocalDateTime.of(2026, 6, 7, 12, 0));
        return payment;
    }
}
