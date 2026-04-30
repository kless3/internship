package com.internship.order_service.unit;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.OrderItemDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.response.UserInfoDto;
import com.internship.order_service.exception.InvalidOrderStatusException;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.exception.UserServiceUnavailableException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.impl.OrderLifecycleServiceImpl;
import com.internship.order_service.service.impl.OrderProcessingServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderProcessingServiceImpl orderProcessingService;

    @InjectMocks
    private OrderLifecycleServiceImpl orderService;

    private Order order;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void setUp() {
        ItemDto itemDto = new ItemDto(1L, "Test Item", new BigDecimal("29.99"));
        OrderItemDto orderItemDto = new OrderItemDto(itemDto, 2L);
        UserInfoDto userInfoDto = new UserInfoDto(1L, "test@example.com");

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUserEmail("test@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("Test address");
        order.setCreationDate(LocalDateTime.now());

        orderResponseDto = new OrderResponseDto(
                1L,
                1L,
                OrderStatus.PENDING,
                LocalDateTime.now(),
                "Test address",
                List.of(orderItemDto),
                userInfoDto
        );
    }

    @Test
    void createOrder_shouldMapAndSave() {
        OrderRequestDto requestDto = new OrderRequestDto(1L, "test@example.com", "Test address", orderResponseDto.orderItems());
        when(orderMapper.toEntity(requestDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.createOrder(requestDto);

        assertEquals(OrderStatus.PENDING, result.status());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.getOrderById(1L);

        assertEquals(orderResponseDto.userId(), result.userId());
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void getOrdersByIds_shouldReturnOrders() {
        when(orderRepository.findByIdIn(List.of(1L))).thenReturn(List.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        List<OrderResponseDto> result = orderService.getOrdersByIds(List.of(1L));

        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByIds_shouldThrowWhenEmpty() {
        when(orderRepository.findByIdIn(List.of(1L))).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByIds(List.of(1L)));
    }

    @Test
    void getOrdersByStatus_shouldReturnOrders() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        List<OrderResponseDto> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByStatus_shouldThrowWhenStatusNull() {
        assertThrows(InvalidOrderStatusException.class, () -> orderService.getOrdersByStatus(null));
    }

    @Test
    void getOrdersByUserEmail_shouldReturnPage() {
        UserInfoDto user = new UserInfoDto(1L, "test@example.com");
        Page<Order> page = new PageImpl<>(List.of(order));

        when(userServiceClient.getUserInfoByEmail("test@example.com")).thenReturn(user);
        when(orderRepository.findAllByUserId(1L, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("creationDate").descending())))
                .thenReturn(page);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);

        Page<OrderResponseDto> result = orderService.getOrdersByUserEmail("test@example.com", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrdersByUserEmail_shouldThrowWhenUserNotFound() {
        when(userServiceClient.getUserInfoByEmail("none@example.com")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByUserEmail("none@example.com", 0, 10));
    }

    @Test
    void updateOrderById_shouldUpdate() {
        OrderRequestDto requestDto = new OrderRequestDto(1L, "test@example.com", "Test address", orderResponseDto.orderItems());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.updateOrderById(1L, requestDto);

        assertEquals(1L, result.id());
    }

    @Test
    void updateShippingAddress_shouldUpdateWhenPending() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.updateShippingAddress(1L, new UpdateShippingAddressRequestDto(" New address "));

        assertEquals(1L, result.id());
        verify(orderRepository).save(order);
    }

    @Test
    void updateShippingAddress_shouldThrowWhenNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderValidationException.class,
                () -> orderService.updateShippingAddress(1L, new UpdateShippingAddressRequestDto("addr")));
    }

    @Test
    void deleteOrderById_shouldDelete() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        orderService.deleteOrderById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrderById(1L));
    }

    @Test
    void getOrderById_shouldWrapFeignException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        doThrow(FeignException.class).when(userServiceClient).getUserInfoByEmail(anyString());

        assertThrows(UserServiceUnavailableException.class, () -> orderService.getOrderById(1L));
    }
}
