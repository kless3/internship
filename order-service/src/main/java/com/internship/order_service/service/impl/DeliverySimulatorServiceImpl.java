package com.internship.order_service.service.impl;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.DeliverySimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DeliverySimulatorServiceImpl implements DeliverySimulatorService {

    private final OrderEventRepository orderEventRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void simulateConfirmation(Order order, OrderEventStatus status) {

        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setUserId(order.getUserId());
        orderEvent.setOrderId(order.getId());
        orderEvent.setUserEmail(order.getUserEmail());

        if (status == OrderEventStatus.PAID_SUCCESS) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderEvent.setStatus(OrderEventStatus.CONFIRMED);
        } else {
            order.setStatus(OrderStatus.FAILED);
            orderEvent.setStatus(OrderEventStatus.REJECTED);
        }

        Order updatedOrder = orderRepository.save(order);
        orderEventRepository.save(orderEvent);

        if (updatedOrder.getStatus() == OrderStatus.CONFIRMED) {
            simulateDelivery(updatedOrder);
        }
    }

    @Override
    @Transactional
    public void simulateDelivery(Order order) {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setUserId(order.getUserId());
        orderEvent.setOrderId(order.getId());
        orderEvent.setUserEmail(order.getUserEmail());

        int randomNumber = generateRandomNumber();

        if (randomNumber % 2 == 0) {
            order.setStatus(OrderStatus.DELIVERED);
            orderEvent.setStatus(OrderEventStatus.DELIVERED);
        } else {
            order.setStatus(OrderStatus.REFUNDED);
            orderEvent.setStatus(OrderEventStatus.REFUNDED);
        }

        orderRepository.save(order);
        orderEventRepository.save(orderEvent);
    }

    private int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(1, 101);
    }
}

