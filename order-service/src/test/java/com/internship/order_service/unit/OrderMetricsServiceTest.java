package com.internship.order_service.unit;

import com.internship.order_service.dto.response.AverageCreateToPayDurationResponseDto;
import com.internship.order_service.dto.response.ShippingAddressChangeFrequencyResponseDto;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.service.impl.OrderMetricsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMetricsServiceTest {

    @Mock
    private OrderEventRepository orderEventRepository;

    @InjectMocks
    private OrderMetricsServiceImpl orderMetricsService;

    @Test
    void getAverageCreateToPayDuration_shouldCalculateAverage() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime payStartedAt = createdAt.plusSeconds(30);

        OrderEvent created = event(1L, OrderEventStatus.CREATED, createdAt);
        OrderEvent paymentStarted = event(1L, OrderEventStatus.PAYMENT_STARTED, payStartedAt);

        when(orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of(created, paymentStarted));

        AverageCreateToPayDurationResponseDto result = orderMetricsService.getAverageCreateToPayDuration(10L);

        assertEquals(1L, result.samplesCount());
        assertEquals(30_000L, result.averageDurationMs());
        assertEquals(new BigDecimal("30.000"), result.averageDurationSeconds());
    }

    @Test
    void getAverageCreateToPayDuration_shouldReturnZeroWhenNoSamples() {
        when(orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of());

        AverageCreateToPayDurationResponseDto result = orderMetricsService.getAverageCreateToPayDuration(10L);

        assertEquals(0L, result.samplesCount());
        assertEquals(0L, result.averageDurationMs());
        assertEquals(BigDecimal.ZERO, result.averageDurationSeconds());
    }

    @Test
    void getShippingAddressChangeFrequency_shouldCalculateRate() {
        LocalDateTime t0 = LocalDateTime.now().minusMinutes(10);
        LocalDateTime t1 = t0.plusMinutes(1);
        LocalDateTime t2 = t0.plusMinutes(2);

        OrderEvent created1 = event(1L, OrderEventStatus.CREATED, t0);
        OrderEvent changed1 = event(1L, OrderEventStatus.SHIPPING_ADDRESS_UPDATED, t1);
        OrderEvent created2 = event(2L, OrderEventStatus.CREATED, t2);

        when(orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of(created1, changed1, created2));

        ShippingAddressChangeFrequencyResponseDto result = orderMetricsService.getShippingAddressChangeFrequency(10L);

        assertEquals(2L, result.totalCreatedOrders());
        assertEquals(1L, result.ordersWithAddressChanges());
        assertEquals(new BigDecimal("50.00"), result.changeRatePercent());
    }

    @Test
    void getShippingAddressChangeFrequency_shouldReturnZeroWhenNoCreatedOrders() {
        when(orderEventRepository.findAllByUserIdAndStatusInOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of());

        ShippingAddressChangeFrequencyResponseDto result = orderMetricsService.getShippingAddressChangeFrequency(10L);

        assertEquals(0L, result.totalCreatedOrders());
        assertEquals(0L, result.ordersWithAddressChanges());
        assertEquals(BigDecimal.ZERO, result.changeRatePercent());
    }

    private OrderEvent event(Long orderId, OrderEventStatus status, LocalDateTime timestamp) {
        OrderEvent e = new OrderEvent();
        e.setOrderId(orderId);
        e.setStatus(status);
        e.setEventTimestamp(timestamp);
        return e;
    }
}
