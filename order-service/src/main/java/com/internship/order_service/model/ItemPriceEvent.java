package com.internship.order_service.model;

import com.internship.order_service.model.enums.ItemPriceEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_price_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPriceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false, updatable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false, updatable = false)
    private String itemName;

    @Column(name = "price", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private ItemPriceEventType eventType;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime eventTimestamp;
}
