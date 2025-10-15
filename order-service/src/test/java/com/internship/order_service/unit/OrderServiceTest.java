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
import java.time.LocalDate;
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
        itemDTO = new ItemDTO();
        itemDTO.setId(1L);
        itemDTO.setName("Test Item");
        itemDTO.setPrice(new BigDecimal("29.99"));

        orderItemDTO = new OrderItemDTO();
        orderItemDTO.setItem(itemDTO);
        orderItemDTO.setQuantity(2L);

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setUserId(1L);
        orderRequestDTO.setUserEmail("test@example.com");
        orderRequestDTO.setOrderItems(List.of(orderItemDTO));

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setUserEmail("test@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setCreationDate(LocalDateTime.now());

        userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName("John");
        userInfoDTO.setSurname("Doe");
        userInfoDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfoDTO.setEmail("test@example.com");

        orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setUserId(1L);
        orderResponseDTO.setStatus(OrderStatus.PENDING);
        orderResponseDTO.setCreationDate(LocalDateTime.now());
        orderResponseDTO.setOrderItems(List.of(orderItemDTO));
        orderResponseDTO.setUserInfoDto(userInfoDTO);
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_ShouldReturnOrderResponseDTO() {
        when(orderMapper.toEntity(orderRequestDTO)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDTO);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals(orderResponseDTO.getUserId(), result.getUserId());
        assertEquals(orderResponseDTO.getUserInfoDto().getEmail(), result.getUserInfoDto().getEmail());
        verify(orderMapper).toEntity(orderRequestDTO);
        verify(orderRepository).save(order);
        verify(orderMapper).toDTO(order);
        verify(userServiceClient).getUserInfoByEmail(order.getUserEmail());
    }

    @Test
    @DisplayName("Should set order reference for order items when creating order")
    void createOrder_WithOrderItems_ShouldSetOrderReference() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setQuantity(2L);


        order.setOrderItems(List.of(orderItem));
        when(orderMapper.toEntity(orderRequestDTO)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderResponseDTO);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        OrderResponseDTO result = orderService.createOrder(orderRequestDTO);

        assertNotNull(result);
        assertEquals(order, orderItem.getOrder());
        verify(orderRepository).save(order);
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
        assertEquals(orderResponseDTO.getUserId(), result.getUserId());
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

        OrderResponseDTO orderResponseDTO2 = new OrderResponseDTO();
        orderResponseDTO2.setUserId(2L);
        orderResponseDTO2.setUserInfoDto(userInfoDTO);

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
        assertEquals(status, result.get(0).getStatus());
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
        OrderRequestDTO updateRequest = new OrderRequestDTO();
        updateRequest.setUserId(1L);
        updateRequest.setUserEmail("updated@example.com");
        updateRequest.setOrderItems(List.of(orderItemDTO));

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setUserId(1L);
        updatedOrder.setUserEmail("updated@example.com");
        updatedOrder.setStatus(OrderStatus.DELIVERED);

        OrderResponseDTO updatedResponse = new OrderResponseDTO();
        updatedResponse.setUserId(orderId);
        updatedResponse.setStatus(OrderStatus.DELIVERED);
        updatedResponse.setUserInfoDto(userInfoDTO);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(updatedOrder);
        when(orderMapper.toDTO(updatedOrder)).thenReturn(updatedResponse);
        when(userServiceClient.getUserInfoByEmail(anyString())).thenReturn(userInfoDTO);

        OrderResponseDTO result = orderService.updateOrderById(orderId, updateRequest);

        assertNotNull(result);
        assertEquals(updatedResponse.getUserId(), result.getUserId());
        assertEquals(updatedResponse.getStatus(), result.getStatus());

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
        OrderRequestDTO updateRequest = new OrderRequestDTO();
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