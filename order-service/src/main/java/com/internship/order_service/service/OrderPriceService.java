package com.internship.order_service.service;

import com.internship.order_service.dto.response.OrderPriceResponseDto;

import java.time.LocalDateTime;

public interface OrderPriceService {

    OrderPriceResponseDto getOrderPriceAt(Long id, LocalDateTime date);
}
