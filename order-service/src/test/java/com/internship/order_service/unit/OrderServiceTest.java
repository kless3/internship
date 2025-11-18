package com.internship.order_service.unit;

import com.internship.order_service.client.UserServiceClient;
import com.internship.order_service.dto.*;
import com.internship.order_service.exception.OrderProcessingException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.mapper.OrderMapper;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderItem;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequestDTO orderRequestDTO;
    private Order order;
    private OrderResponseDTO orderResponseDTO;
    private UserInfoDTO userInfoDTO;
    private OrderItemDTO orderItemDTO;
    private ItemDTO itemDTO;

    @BeforeEach
    void setUp() {
        itemDTO = new ItemDTO(1L, "Test Item", new BigDecimal("29.99"));
        orderItemDTO = new OrderItemDTO(itemDTO, 2L);
        orderRequestDTO = new OrderRequestDTO(1L, "test@example.com", List.of(orderItemDTO));

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUserEmail("test@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setCreationDate(LocalDateTime.now());

        userInfoDTO = new UserInfoDTO(1L, "test@example.com");
        orderResponseDTO = new OrderResponseDTO(1L, OrderStatus.PENDING, LocalDateTime.now(), List.of(orderItemDTO), userInfoDTO);
    }

    @Test
    @DisplayName("Should get order by id successfully")
    void getOrderById_ShouldReturnOrderResponseDTO() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDTO);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        OrderResponseDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderResponseDTO.userId(), result.userId());
        verify(orderRepository).findById(orderId);
        verify(orderMapper).toDTO(order);
        verify(userServiceClient).getUserInfoByEmail(order.getUserEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by id")
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(orderId));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderMapper, never()).toDTO(any());
        verify(userServiceClient, never()).getUserInfoByEmail(anyString());
    }

    @Test
    @DisplayName("Should get orders by list of ids successfully")
    void getOrdersByIds_ShouldReturnListOfOrderResponseDTO() {
        List<Long> orderIds = Arrays.asList(1L, 2L);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(2L);
        order2.setUserEmail("test2@example.com");
        List<Order> orders = Arrays.asList(order, order2);

        OrderResponseDTO orderResponseDTO2 = new OrderResponseDTO(2L, OrderStatus.PENDING, LocalDateTime.now(), List.of(orderItemDTO), userInfoDTO);

        when(orderRepository.findByIdIn(orderIds)).thenReturn(orders);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDTO);
        when(orderMapper.toDTO(order2)).thenReturn(orderResponseDTO2);
        when(userServiceClient.getUserInfoByEmail("test@example.com")).thenReturn(userInfoDTO);
        when(userServiceClient.getUserInfoByEmail("test2@example.com")).thenReturn(userInfoDTO);

        List<OrderResponseDTO> result = orderService.getOrdersByIds(orderIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findByIdIn(orderIds);
        verify(orderMapper, times(2)).toDTO(any(Order.class));
        verify(userServiceClient, times(2)).getUserInfoByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no orders found by ids")
    void getOrdersByIds_WhenNoOrdersFound_ShouldThrowException() {
        List<Long> orderIds = Arrays.asList(999L, 1000L);
        when(orderRepository.findByIdIn(orderIds)).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByIds(orderIds));

        assertEquals("Orders not found with ids: [999, 1000]", exception.getMessage());
        verify(orderRepository).findByIdIn(orderIds);
        verify(orderMapper, never()).toDTO(any(Order.class));
        verify(userServiceClient, never()).getUserInfoByEmail(anyString());
    }

    @Test
    @DisplayName("Should get orders by status successfully")
    void getOrdersByStatus_ShouldReturnListOfOrderResponseDTO() {
        OrderStatus status = OrderStatus.PENDING;
        List<Order> orders = Arrays.asList(order);

        when(orderRepository.findByStatus(status)).thenReturn(orders);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDTO);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        List<OrderResponseDTO> result = orderService.getOrdersByStatus(status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).status());
        verify(orderRepository).findByStatus(status);
        verify(orderMapper).toDTO(order);
        verify(userServiceClient).getUserInfoByEmail(order.getUserEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no orders found by status")
    void getOrdersByStatus_WhenNoOrdersFound_ShouldThrowException() {
        OrderStatus status = OrderStatus.DELIVERED;
        when(orderRepository.findByStatus(status)).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByStatus(status));

        assertEquals("Orders not found with status: DELIVERED", exception.getMessage());
        verify(orderRepository).findByStatus(status);
        verify(orderMapper, never()).toDTO(any(Order.class));
        verify(userServiceClient, never()).getUserInfoByEmail(anyString());
    }

    @Test
    @DisplayName("Should update order successfully")
    void updateOrderById_ShouldReturnUpdatedOrderResponseDTO() {
        Long orderId = 1L;
        OrderRequestDTO updateRequest = new OrderRequestDTO(1L, "updated@example.com", List.of(orderItemDTO));

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUserId(1L);
        updatedOrder.setUserEmail("updated@example.com");
        updatedOrder.setStatus(OrderStatus.DELIVERED);

        OrderResponseDTO updatedResponse = new OrderResponseDTO(orderId, OrderStatus.DELIVERED, LocalDateTime.now(), List.of(orderItemDTO), userInfoDTO);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);
        when(orderMapper.toDTO(updatedOrder)).thenReturn(updatedResponse);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        OrderResponseDTO result = orderService.updateOrderById(orderId, updateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.userId(), result.userId());
        assertEquals(updatedResponse.status(), result.status());

        verify(orderRepository).findById(orderId);
        verify(orderMapper).updateEntityFromDTO(updateRequest, order);
        verify(orderRepository).save(order);
        verify(orderMapper).toDTO(updatedOrder);
        verify(userServiceClient).getUserInfoByEmail(updatedOrder.getUserEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent order")
    void updateOrderById_WhenOrderNotFound_ShouldThrowException() {
        Long orderId = 999L;
        OrderRequestDTO updateRequest = new OrderRequestDTO(1L, "test@example.com", List.of(orderItemDTO));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderProcessingException exception = assertThrows(OrderProcessingException.class,
                () -> orderService.updateOrderById(orderId, updateRequest));

        assertEquals("Failed to update order", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderMapper, never()).updateEntityFromDTO(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete order successfully")
    void deleteOrderById_ShouldDeleteOrder() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).deleteById(orderId);

        orderService.deleteOrderById(orderId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent order")
    void deleteOrderById_WhenOrderNotFound_ShouldThrowException() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.deleteOrderById(orderId));

        assertEquals("Order not found with id: 999", exception.getMessage());
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).deleteById(orderId);
    }
}