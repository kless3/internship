package com.internship.order_service.controller;

import com.internship.order_service.dto.response.AverageCreateToPayDurationResponseDto;
import com.internship.order_service.dto.response.ShippingAddressChangeFrequencyResponseDto;
import com.internship.order_service.service.OrderMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class OrderMetricsController {

    private final OrderMetricsService orderMetricsService;

    @GetMapping("/customers/{userId}/averageDuration")
    public ResponseEntity<AverageCreateToPayDurationResponseDto> getAverageCreateToPayDuration(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(orderMetricsService.getAverageCreateToPayDuration(userId));
    }

    @GetMapping("/customers/{userId}/shippingAddressChangeFrequency")
    public ResponseEntity<ShippingAddressChangeFrequencyResponseDto> getShippingAddressChangeFrequency(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(orderMetricsService.getShippingAddressChangeFrequency(userId));
    }
}

