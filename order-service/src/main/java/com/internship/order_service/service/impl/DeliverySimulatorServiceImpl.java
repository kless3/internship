package com.internship.order_service.service.impl;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.DeliverySimulatorService;
import com.internship.order_service.service.OrderEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DeliverySimulatorServiceImpl implements DeliverySimulatorService {

    private final OrderEventService orderEventService;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void simulateConfirmation(Order order, OrderEventStatus status) {

        OrderEventStatus confirmationResult;
        if (status == OrderEventStatus.PAID_SUCCESS) {
            order.setStatus(OrderStatus.CONFIRMED);
            confirmationResult = OrderEventStatus.CONFIRMED;
        } else {
            order.setStatus(OrderStatus.FAILED);
            confirmationResult = OrderEventStatus.REJECTED;
        }

        Order updatedOrder = orderRepository.save(order);
        orderEventService.saveConfirmationResult(updatedOrder, confirmationResult);

        if (updatedOrder.getStatus() == OrderStatus.CONFIRMED) {
            simulateDelivery(updatedOrder);
        }
    }

    @Override
    @Transactional
    public void simulateDelivery(Order order) {
        int randomNumber = generateRandomNumber();

        OrderEventStatus deliveryResult;
        if (randomNumber % 2 == 0) {
            order.setStatus(OrderStatus.DELIVERED);
            deliveryResult = OrderEventStatus.DELIVERED;
        } else {
            order.setStatus(OrderStatus.REFUNDED);
            deliveryResult = OrderEventStatus.REFUNDED;
        }

        Order updatedOrder = orderRepository.save(order);
        orderEventService.saveDeliveryResult(updatedOrder, deliveryResult, randomNumber);
    }

    private int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(1, 101);
    }
}
