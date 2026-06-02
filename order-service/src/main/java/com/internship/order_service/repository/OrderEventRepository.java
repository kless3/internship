package com.internship.order_service.repository;

import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    List<OrderEvent> findAllByOrderIdOrderByEventTimestampAsc(Long orderId);

    Optional<OrderEvent> findTopByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampDesc(
            Long orderId,
            LocalDateTime eventTimestamp
    );

    List<OrderEvent> findAllByOrderIdAndEventTimestampLessThanEqualOrderByEventTimestampAsc(
            Long orderId,
            LocalDateTime eventTimestamp
    );

    List<OrderEvent> findAllByUserIdAndStatusInOrderByEventTimestampAsc(
            Long userId,
            List<OrderEventStatus> statuses
    );
}

