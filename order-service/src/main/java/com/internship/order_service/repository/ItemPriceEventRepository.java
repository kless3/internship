package com.internship.order_service.repository;

import com.internship.order_service.model.ItemPriceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemPriceEventRepository extends JpaRepository<ItemPriceEvent, Long> {

    List<ItemPriceEvent> findAllByItemIdAndEventTimestampGreaterThanEqualOrderByEventTimestampAsc(
            Long itemId,
            LocalDateTime fromTimestamp
    );

    ItemPriceEvent findTopByItemIdAndEventTimestampLessThanOrderByEventTimestampDesc(
            Long itemId,
            LocalDateTime timestamp
    );

    ItemPriceEvent findTopByItemIdAndEventTimestampLessThanEqualOrderByEventTimestampDesc(
            Long itemId,
            LocalDateTime timestamp
    );

    ItemPriceEvent findTopByItemIdOrderByEventTimestampAsc(Long itemId);
}
