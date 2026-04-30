package com.internship.order_service.service;

import com.internship.order_service.dto.response.AverageCreateToPayDurationResponseDto;
import com.internship.order_service.dto.response.ShippingAddressChangeFrequencyResponseDto;

public interface OrderMetricsService {

    AverageCreateToPayDurationResponseDto getAverageCreateToPayDuration(Long userId);

    ShippingAddressChangeFrequencyResponseDto getShippingAddressChangeFrequency(Long userId);
}

