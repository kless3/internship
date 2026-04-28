package com.internship.order_service.unit;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.OrderItemDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.dto.response.UserInfoDto;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.kafka.OrderEventProducer;
import com.internship.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventRepository orderEventRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

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
    void getOrderById_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.getOrderById(1L);

        assertEquals(orderResponseDto.userId(), result.userId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals("Order not found with id: 999", ex.getMessage());
    }

    @Test
    void createOrder_shouldMapAndSave() {
        OrderRequestDto requestDto = new OrderRequestDto(
                1L,
                "test@example.com",
                "Test address",
                orderResponseDto.orderItems()
        );
        when(orderMapper.toEntity(requestDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDto);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(orderResponseDto.userInfoDto());

        OrderResponseDto result = orderService.createOrder(requestDto);

        assertEquals(OrderStatus.PENDING, result.status());
        verify(orderRepository).save(order);
    }
}
