package com.internship.payment_service.service.impl;

import com.internship.payment_service.config.property.S3Properties;
import com.internship.payment_service.exception.PaymentReceiptException;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.service.PaymentReceiptService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentReceiptServiceImpl implements PaymentReceiptService {

    private static final String RECEIPT_TEMPLATE = "receipt";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String RECEIPT_CREATION_ERROR = "Failed to create payment receipt";
    private static final String RECEIPT_DOWNLOAD_ERROR = "Failed to download payment receipt";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TemplateEngine templateEngine;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public String createReceipt(Payment payment) {
        try {
            String receiptKey = createReceiptKey(payment);
            byte[] receiptPdf = renderReceiptPdf(payment, receiptKey);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getReceiptBucketName())
                    .key(receiptKey)
                    .contentType(PDF_CONTENT_TYPE)
                    .contentLength((long) receiptPdf.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(receiptPdf));

            return receiptKey;
        } catch (Exception e) {
            throw new PaymentReceiptException(RECEIPT_CREATION_ERROR, e);
        }
    }

    @Override
    public byte[] getReceipt(String receiptKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Properties.getReceiptBucketName())
                    .key(receiptKey)
                    .build();

            ResponseBytes<GetObjectResponse> receipt = s3Client.getObjectAsBytes(request);
            return receipt.asByteArray();
        } catch (Exception e) {
            throw new PaymentReceiptException(RECEIPT_DOWNLOAD_ERROR, e);
        }
    }

    private byte[] renderReceiptPdf(Payment payment, String receiptKey) throws Exception {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("receiptNumber", createReceiptNumber(payment));
        context.setVariable("generatedAt", formatDateTime(LocalDateTime.now()));
        context.setVariable("paymentStatus", payment.getStatus());
        context.setVariable("statusMessage", resolveStatusMessage(payment));
        context.setVariable("paymentId", payment.getId());
        context.setVariable("paymentCreatedAt", formatDateTime(payment.getTimestamp()));
        context.setVariable("paymentAmount", payment.getPaymentAmount());
        context.setVariable("receiptKey", receiptKey);

        String html = templateEngine.process(RECEIPT_TEMPLATE, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        }
    }

    private String createReceiptKey(Payment payment) {
        return "receipts/order-%s/payment-%s.pdf".formatted(payment.getOrderId(), payment.getId());
    }

    private String createReceiptNumber(Payment payment) {
        return "RCPT-%s".formatted(payment.getId());
    }

    private String resolveStatusMessage(Payment payment) {
        if (payment.getStatus() == null) {
            return "Payment status unknown";
        }

        return switch (payment.getStatus()) {
            case COMPLETED -> "Payment completed";
            case FAILED -> "Payment failed";
            case CANCELLED -> "Payment cancelled";
            case PENDING -> "Payment pending";
            case PROCESSING -> "Payment processing";
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
