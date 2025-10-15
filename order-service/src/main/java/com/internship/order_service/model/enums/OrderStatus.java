package com.internship.order_service.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {

    PENDING("Pending processing"),
    CONFIRMED("Confirmed"),
    PROCESSING("In processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refund processed"),
    FAILED("Processing error");

    private final String description;

}
