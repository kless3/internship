package com.internship.payment_service.service.impl;

import com.internship.payment_service.config.property.S3Properties;
import com.internship.payment_service.exception.PaymentReceiptCreationException;
import com.internship.payment_service.exception.PaymentReceiptDownloadException;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.service.PaymentReceiptService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    private final S3Template s3Template;
    private final S3Properties s3Properties;

    @Override
    public String createReceipt(Payment payment) {
        try {
            String receiptKey = createReceiptKey(payment);
            byte[] receiptPdf = renderReceiptPdf(payment, receiptKey);
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(PDF_CONTENT_TYPE)
                    .build();

            s3Template.upload(
                    s3Properties.getReceiptBucketName(),
                    receiptKey,
                    new ByteArrayInputStream(receiptPdf),
                    metadata
            );

            return receiptKey;
        } catch (Exception e) {
            throw new PaymentReceiptCreationException(RECEIPT_CREATION_ERROR, e);
        }
    }

    @Override
    public byte[] getReceipt(String receiptKey) {
        try (InputStream inputStream = s3Template.download(s3Properties.getReceiptBucketName(), receiptKey).getInputStream()) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new PaymentReceiptDownloadException(RECEIPT_DOWNLOAD_ERROR, e);
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
