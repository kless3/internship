package com.internship.order_service.repository;

import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByIdIn(List<Long> ids);
    List<Order> findByStatus(OrderStatus status);
}