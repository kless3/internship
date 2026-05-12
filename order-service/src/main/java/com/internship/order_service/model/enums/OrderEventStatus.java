package com.internship.order_service.model.enums;

public enum OrderEventStatus {
    CREATED,
    SHIPPING_ADDRESS_UPDATED,

    PAYMENT_CANCELLED,
    PAYMENT_STARTED,

    PAID_SUCCESS,
    PAID_FAILED,

    CONFIRMED,
    REJECTED,

    DELIVERED,
    REFUNDED,

    RESTORED,

    DISCOUNT_APPLIED,
    DISCOUNT_REMOVED
}
