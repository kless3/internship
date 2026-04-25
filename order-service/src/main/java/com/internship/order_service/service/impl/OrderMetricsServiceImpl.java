package com.internship.order_service.service.impl;

import com.internship.order_service.dto.AverageCreateToPayDurationResponseDto;
import com.internship.order_service.dto.ShippingAddressChangeFrequencyResponseDto;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.service.OrderMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderMetricsServiceImpl implements OrderMetricsService {

    private static final Set<OrderEventStatus> CREATE_TO_PAY_TRACKED_STATUSES = EnumSet.of(OrderEventStatus.CREATED, OrderEventStatus.PAYMENT_STARTED);
    private static final Set<OrderEventStatus> SHIPPING_ADDRESS_TRACKED_STATUSES = EnumSet.of(OrderEventStatus.CREATED, OrderEventStatus.SHIPPING_ADDRESS_UPDATED);

    private final OrderEventRepository orderEventRepository;

    @Override
    @Transactional(readOnly = true)
    public AverageCreateToPayDurationResponseDto getAverageCreateToPayDuration(Long userId) {
        List<OrderEvent> events = orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(
                userId,
                CREATE_TO_PAY_TRACKED_STATUSES.stream().toList()
        );

        Map<Long, LocalDateTime> createdAtByOrderId = new HashMap<>();
        Map<Long, LocalDateTime> payStartedAtByOrderId = new HashMap<>();

        for (OrderEvent event : events) {
            Long orderId = event.getOrderId();
            if (orderId == null || event.getEventTimestamp() == null || event.getStatus() == null) {
                continue;
            }

            if (event.getStatus() == OrderEventStatus.CREATED) {
                createdAtByOrderId.putIfAbsent(orderId, event.getEventTimestamp());
                continue;
            }

            if (event.getStatus() == OrderEventStatus.PAYMENT_STARTED) {
                LocalDateTime createdAt = createdAtByOrderId.get(orderId);
                if (createdAt == null || event.getEventTimestamp().isBefore(createdAt)) {
                    continue;
                }
                payStartedAtByOrderId.putIfAbsent(orderId, event.getEventTimestamp());
            }
        }

        long totalDurationMs = 0L;
        long samplesCount = 0L;

        for (Map.Entry<Long, LocalDateTime> entry : payStartedAtByOrderId.entrySet()) {
            LocalDateTime createdAt = createdAtByOrderId.get(entry.getKey());
            LocalDateTime payStartedAt = entry.getValue();
            if (createdAt == null || payStartedAt == null || payStartedAt.isBefore(createdAt)) {
                continue;
            }

            totalDurationMs += Duration.between(createdAt, payStartedAt).toMillis();
            samplesCount++;
        }

        if (samplesCount == 0L) {
            return new AverageCreateToPayDurationResponseDto(0L, 0L, BigDecimal.ZERO);
        }

        long averageDurationMs = totalDurationMs / samplesCount;
        BigDecimal averageDurationSeconds = BigDecimal.valueOf(averageDurationMs)
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        return new AverageCreateToPayDurationResponseDto(samplesCount, averageDurationMs, averageDurationSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingAddressChangeFrequencyResponseDto getShippingAddressChangeFrequency(Long userId) {
        List<OrderEvent> events = orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(
                userId,
                SHIPPING_ADDRESS_TRACKED_STATUSES.stream().toList()
        );

        Map<Long, LocalDateTime> createdAtByOrderId = new HashMap<>();
        Map<Long, Boolean> hasAddressChangeByOrderId = new HashMap<>();

        for (OrderEvent event : events) {
            Long orderId = event.getOrderId();
            if (orderId == null || event.getEventTimestamp() == null || event.getStatus() == null) {
                continue;
            }

            if (event.getStatus() == OrderEventStatus.CREATED) {
                createdAtByOrderId.putIfAbsent(orderId, event.getEventTimestamp());
                hasAddressChangeByOrderId.putIfAbsent(orderId, false);
                continue;
            }

            if (event.getStatus() == OrderEventStatus.SHIPPING_ADDRESS_UPDATED) {
                LocalDateTime createdAt = createdAtByOrderId.get(orderId);
                if (createdAt == null || event.getEventTimestamp().isBefore(createdAt)) {
                    continue;
                }
                hasAddressChangeByOrderId.put(orderId, true);
            }
        }

        long totalCreatedOrders = createdAtByOrderId.size();
        if (totalCreatedOrders == 0L) {
            return new ShippingAddressChangeFrequencyResponseDto(0L, 0L, BigDecimal.ZERO);
        }

        long ordersWithAddressChanges = hasAddressChangeByOrderId.values().stream()
                .filter(Boolean.TRUE::equals)
                .count();

        BigDecimal changeRatePercent = BigDecimal.valueOf(ordersWithAddressChanges)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCreatedOrders), 2, RoundingMode.HALF_UP);

        return new ShippingAddressChangeFrequencyResponseDto(
                totalCreatedOrders,
                ordersWithAddressChanges,
                changeRatePercent
        );
    }
}
