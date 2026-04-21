package com.internship.order_service.repository;

import com.internship.order_service.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findAllByOrderIdOrderByEventTimestampAsc(Long orderId);
}
