package com.internship.order_service.model.payload;

import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OrderEventPayload {

    private static final String DISCOUNT_PERCENT_PAYLOAD_KEY = "discountPercent";
    private static final String SHIPPING_ADDRESS_PAYLOAD_KEY = "shippingAddress";
    private static final String PAYMENT_REQUEST_TOTAL_AMOUNT_PAYLOAD_KEY = "paymentRequestTotalAmount";
    private static final String PAYMENT_ID_PAYLOAD_KEY = "paymentId";
    private static final String PAYMENT_STATUS_PAYLOAD_KEY = "paymentStatus";
    private static final String PAYMENT_AMOUNT_PAYLOAD_KEY = "paymentAmount";
    private static final String PAYMENT_CREATED_AT_PAYLOAD_KEY = "paymentCreatedAt";
    private static final String RESTORE_TIMESTAMP_PAYLOAD_KEY = "restoreTimestamp";
    private static final String CONFIRMATION_RESULT_PAYLOAD_KEY = "confirmationResult";
    private static final String DELIVERY_RESULT_PAYLOAD_KEY = "deliveryResult";
    private static final String DELIVERY_RANDOM_NUMBER_PAYLOAD_KEY = "deliveryRandomNumber";
    private static final String PAYMENT_STATUS_STARTED = "STARTED";

    private OrderEventPayload() {
    }

    public static Map<String, Object> addressSnapshot(String shippingAddress) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, SHIPPING_ADDRESS_PAYLOAD_KEY, shippingAddress);
        return emptyToNull(payload);
    }

    public static Map<String, Object> paymentStarted(BigDecimal totalAmount) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, PAYMENT_STATUS_PAYLOAD_KEY, PAYMENT_STATUS_STARTED);
        put(payload, PAYMENT_REQUEST_TOTAL_AMOUNT_PAYLOAD_KEY, totalAmount);
        return emptyToNull(payload);
    }

    public static Map<String, Object> paymentCreated(String paymentId,
                                                     String paymentStatus,
                                                     BigDecimal paymentAmount,
                                                     LocalDateTime paymentCreatedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, PAYMENT_ID_PAYLOAD_KEY, paymentId);
        put(payload, PAYMENT_STATUS_PAYLOAD_KEY, paymentStatus);
        put(payload, PAYMENT_AMOUNT_PAYLOAD_KEY, paymentAmount);
        putDateTime(payload, PAYMENT_CREATED_AT_PAYLOAD_KEY, paymentCreatedAt);
        return emptyToNull(payload);
    }

    public static Map<String, Object> restoreTimestamp(LocalDateTime restoreTimestamp) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putDateTime(payload, RESTORE_TIMESTAMP_PAYLOAD_KEY, restoreTimestamp);
        return emptyToNull(payload);
    }

    public static Map<String, Object> discount(BigDecimal discountPercent) {
        Map<String, Object> payload = new LinkedHashMap<>();
        put(payload, DISCOUNT_PERCENT_PAYLOAD_KEY, discountPercent);
        return emptyToNull(payload);
    }

    public static Map<String, Object> confirmationResult(OrderEventStatus confirmationResult) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putStatus(payload, CONFIRMATION_RESULT_PAYLOAD_KEY, confirmationResult);
        return emptyToNull(payload);
    }

    public static Map<String, Object> deliveryResult(OrderEventStatus deliveryResult, Integer randomNumber) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putStatus(payload, DELIVERY_RESULT_PAYLOAD_KEY, deliveryResult);
        put(payload, DELIVERY_RANDOM_NUMBER_PAYLOAD_KEY, randomNumber);
        return emptyToNull(payload);
    }

    public static String getShippingAddress(OrderEvent event) {
        Object value = getPayloadValue(event, SHIPPING_ADDRESS_PAYLOAD_KEY);
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public static BigDecimal getDiscountPercent(OrderEvent event) {
        Object value = getPayloadValue(event, DISCOUNT_PERCENT_PAYLOAD_KEY);
        if (value == null) {
            return null;
        }

        return new BigDecimal(value.toString());
    }

    private static void put(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private static void putDateTime(Map<String, Object> payload, String key, LocalDateTime value) {
        if (value != null) {
            payload.put(key, value.toString());
        }
    }

    private static void putStatus(Map<String, Object> payload, String key, OrderEventStatus value) {
        if (value != null) {
            payload.put(key, value.name());
        }
    }

    private static Object getPayloadValue(OrderEvent event, String key) {
        if (event == null || event.getPayload() == null) {
            return null;
        }

        return event.getPayload().get(key);
    }

    private static Map<String, Object> emptyToNull(Map<String, Object> payload) {
        if (payload.isEmpty()) {
            return null;
        }

        return payload;
    }
}
