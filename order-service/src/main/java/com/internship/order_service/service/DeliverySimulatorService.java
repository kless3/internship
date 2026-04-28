package com.internship.order_service.service;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderEventStatus;

public interface DeliverySimulatorService {

    void simulateConfirmation(Order order, OrderEventStatus status);
    void simulateDelivery(Order order);
}

