package com.internship.order_service.model;

import com.internship.order_service.model.enums.OrderEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false, updatable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = false)
    private OrderEventStatus status;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime eventTimestamp;

    @Column(name = "discount_percent", updatable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;
}
