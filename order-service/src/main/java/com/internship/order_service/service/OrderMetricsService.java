package com.internship.order_service.service;

import com.internship.order_service.dto.AverageCreateToPayDurationResponseDto;

public interface OrderMetricsService {

    AverageCreateToPayDurationResponseDto getAverageCreateToPayDuration(Long userId);
}
