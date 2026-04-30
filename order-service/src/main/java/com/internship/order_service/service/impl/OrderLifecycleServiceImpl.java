package com.internship.order_service.service.impl;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.response.UserInfoDto;
import com.internship.order_service.exception.InvalidOrderStatusException;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderLifecycleService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderLifecycleServiceImpl implements OrderLifecycleService {

    private static final String ORDER_NOT_FOUND_WITH_ID = "Order not found with id: ";
    private static final String ORDER_NOT_FOUND_WITH_IDS = "Orders not found with ids: ";
    private static final String ORDER_NOT_FOUND_WITH_STATUS = "Orders not found with status: ";
    private static final String USER_SERVICE_UNAVAILABLE = "User service is currently unavailable";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";
    private static final String ORDER_STATUS_NULL = "Order status cannot be null";
    private static final String FAILED_TO_CREATE_ORDER = "Failed to create order";
    private static final String FAILED_TO_UPDATE_ORDER = "Failed to update order";
    private static final String ONLY_PENDING_ORDER_CAN_UPDATE_SHIPPING_ADDRESS = "Shipping address can be updated only while order is pending";
    private static final String FAILED_TO_UPDATE_SHIPPING_ADDRESS = "Failed to update shipping address";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final OrderProcessingServiceImpl orderProcessingService;

    @Override
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        try {
            Order order = orderMapper.toEntity(orderRequestDto);
            order.setStatus(OrderStatus.PENDING);

            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(orderItem -> orderItem.setOrder(order));
            }

            Order savedOrder = orderRepository.save(order);
            orderProcessingService.saveEvent(savedOrder, OrderEventStatus.CREATED);

            return toOrderResponseDTO(savedOrder);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_CREATE_ORDER, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        try {
            return toOrderResponseDTO(order);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findByIdIn(ids);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_IDS + ids);
        }

        try {
            return orders.stream().map(this::toOrderResponseDTO).toList();
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus orderStatus) {
        validateOrderStatus(orderStatus);

        List<Order> orders = orderRepository.findByStatus(orderStatus);
        if (orders == null || orders.isEmpty()) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_STATUS + orderStatus);
        }

        try {
            return orders.stream().map(this::toOrderResponseDTO).toList();
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrdersByUserEmail(String userEmail, int page, int size) {
        try {
            UserInfoDto user = userServiceClient.getUserInfoByEmail(userEmail);

            if (user == null || user.id() == null) {
                throw new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + userEmail);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
            return orderRepository.findAllByUserId(user.id(), pageable)
                    .map(order -> {
                        OrderResponseDto orderResponseDto = orderMapper.toDTO(order);
                        return new OrderResponseDto(
                                orderResponseDto.id(),
                                orderResponseDto.userId(),
                                orderResponseDto.status(),
                                orderResponseDto.creationDate(),
                                orderResponseDto.shippingAddress(),
                                orderResponseDto.orderItems(),
                                user
                        );
                    });

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        }
    }

    @Override
    public OrderResponseDto updateOrderById(Long id, OrderRequestDto orderRequestDto) {
        try {
            Order existingOrder = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            orderMapper.updateEntityFromDTO(orderRequestDto, existingOrder);
            Order savedOrder = orderRepository.save(existingOrder);

            return toOrderResponseDTO(savedOrder);
        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_UPDATE_ORDER, e);
        }
    }

    @Override
    public OrderResponseDto updateShippingAddress(Long id, UpdateShippingAddressRequestDto requestDto) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new OrderValidationException(ONLY_PENDING_ORDER_CAN_UPDATE_SHIPPING_ADDRESS);
            }

            order.setShippingAddress(requestDto.shippingAddress().trim());
            Order savedOrder = orderRepository.save(order);
            orderProcessingService.saveEvent(savedOrder, OrderEventStatus.SHIPPING_ADDRESS_UPDATED);

            return toOrderResponseDTO(savedOrder);

        } catch (FeignException e) {
            throw new UserServiceUnavailableException(USER_SERVICE_UNAVAILABLE, e);
        } catch (OrderValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(FAILED_TO_UPDATE_SHIPPING_ADDRESS, e);
        }
    }

    @Override
    public void deleteOrderById(Long id) {
        orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND_WITH_ID + id));

        orderRepository.deleteById(id);
    }

    private OrderResponseDto toOrderResponseDTO(Order order) {
        OrderResponseDto orderResponseDto = orderMapper.toDTO(order);
        UserInfoDto userInfo = userServiceClient.getUserInfoByEmail(order.getUserEmail());
        return new OrderResponseDto(
                orderResponseDto.id(),
                orderResponseDto.userId(),
                orderResponseDto.status(),
                orderResponseDto.creationDate(),
                orderResponseDto.shippingAddress(),
                orderResponseDto.orderItems(),
                userInfo
        );
    }

    private void validateOrderStatus(OrderStatus status) {
        if (status == null) {
            throw new InvalidOrderStatusException(ORDER_STATUS_NULL);
        }
    }
}
