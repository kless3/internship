package com.internship.order_service.service;

import com.internship.order_service.dto.AverageCreateToPayDurationResponseDto;
import com.internship.order_service.dto.ShippingAddressChangeFrequencyResponseDto;

public interface OrderMetricsService {

    AverageCreateToPayDurationResponseDto getAverageCreateToPayDuration(Long userId);

    ShippingAddressChangeFrequencyResponseDto getShippingAddressChangeFrequency(Long userId);
}
