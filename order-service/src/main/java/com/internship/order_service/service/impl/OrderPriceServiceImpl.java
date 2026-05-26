package com.internship.order_service.service.impl;

import com.internship.order_service.dto.response.OrderPriceItemResponseDto;
import com.internship.order_service.dto.response.OrderPriceResponseDto;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.model.Item;
import com.internship.order_service.model.ItemPriceEvent;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.OrderItem;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.payload.OrderEventPayload;
import com.internship.order_service.repository.ItemPriceEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderEventService;
import com.internship.order_service.service.OrderPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderPriceServiceImpl implements OrderPriceService {

    private static final String ORDER_NOT_FOUND_WITH_ID = "Order not found with id: ";
    private static final String PRICE_TIME_REQUIRED = "Price timestamp is required";
    private static final String PRICE_TIME_IN_FUTURE = "Price timestamp cannot be in the future";
    private static final String NO_HISTORICAL_STATE_AT_TIME = "No historical order state found at timestamp: ";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int MONEY_SCALE = 2;

    private final OrderRepository orderRepository;
    private final OrderEventService orderEventService;
    private final ItemPriceEventRepository itemPriceEventRepository;

    @Override
    public OrderPriceResponseDto getOrderPriceAt(Long id, LocalDateTime date) {
        if (date == null) {
            throw new OrderValidationException(PRICE_TIME_REQUIRED);
        }
        if (date.isAfter(LocalDateTime.now())) {
            throw new OrderValidationException(PRICE_TIME_IN_FUTURE);
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        List<OrderEvent> history = orderEventService.getOrderHistoryUntil(id, date);

        if (history.isEmpty()) {
            throw new OrderValidationException(NO_HISTORICAL_STATE_AT_TIME + date);
        }

        List<OrderPriceItemResponseDto> items = order.getOrderItems().stream()
                .map(orderItem -> toPriceItem(orderItem, date))
                .toList();

        BigDecimal subtotal = items.stream()
                .map(OrderPriceItemResponseDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal discountPercent = resolveDiscountPercent(history);
        BigDecimal total = applyDiscount(subtotal, discountPercent);

        return new OrderPriceResponseDto(
                order.getId(),
                date,
                subtotal,
                discountPercent,
                total,
                items
        );
    }

    private OrderPriceItemResponseDto toPriceItem(OrderItem orderItem, LocalDateTime date) {
        Item item = orderItem.getItem();
        BigDecimal unitPrice = resolveItemPrice(item, date).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal subtotal = unitPrice
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new OrderPriceItemResponseDto(
                item.getId(),
                item.getName(),
                orderItem.getQuantity(),
                unitPrice,
                subtotal
        );
    }

    private BigDecimal resolveItemPrice(Item item, LocalDateTime date) {
        ItemPriceEvent priceEvent = itemPriceEventRepository
                .findTopByItemIdAndEventTimestampLessThanEqualOrderByEventTimestampDesc(item.getId(), date);

        if (priceEvent == null) {
            ItemPriceEvent firstKnownPriceEvent = itemPriceEventRepository
                    .findTopByItemIdOrderByEventTimestampAsc(item.getId());
            if (firstKnownPriceEvent == null) {
                return item.getPrice();
            }
            return firstKnownPriceEvent.getPrice();
        }

        return priceEvent.getPrice();
    }

    private BigDecimal resolveDiscountPercent(List<OrderEvent> history) {
        BigDecimal discountPercent = BigDecimal.ZERO;

        for (OrderEvent event : history) {
            if (event.getStatus() == OrderEventStatus.DISCOUNT_APPLIED ||
                    event.getStatus() == OrderEventStatus.DISCOUNT_REMOVED) {

                BigDecimal eventDiscountPercent = OrderEventPayload.getDiscountPercent(event);
                if (eventDiscountPercent == null) {
                    discountPercent = BigDecimal.ZERO;
                } else {
                    discountPercent = eventDiscountPercent;
                }

            }
        }

        return discountPercent;
    }

    private BigDecimal applyDiscount(BigDecimal subtotal, BigDecimal discountPercent) {
        BigDecimal multiplier = ONE_HUNDRED.subtract(discountPercent)
                .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);

        return subtotal.multiply(multiplier).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
