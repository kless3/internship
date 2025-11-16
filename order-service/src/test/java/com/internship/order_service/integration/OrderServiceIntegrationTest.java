package com.internship.order_service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.internship.order_service.dto.ItemDTO;
import com.internship.order_service.dto.OrderItemDTO;
import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@DisplayName("Order Service Integration Tests")
class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "test@example.com";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found with id: ";

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        String wireMockUrl = "http://localhost:" + wireMockServer.port();
        registry.add("user.service.url", () -> wireMockUrl);
    }

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        wireMockServer.resetAll();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @Test
    @DisplayName("Should create order when user exists")
    void shouldCreateOrderWhenUserExists() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        OrderResponseDTO orderResponse = orderService.createOrder(orderRequest);

        assertThat(orderResponse.userId()).isNotNull();
        assertThat(orderResponse.userInfoDto().email()).isEqualTo(USER_EMAIL);
        assertThat(orderResponse.status()).isEqualTo(OrderStatus.PENDING);

        verify(getRequestedFor(urlPathMatching("/api/v1/users/email/.*")));
    }

    @Test
    @DisplayName("Should throw exception when get order by id and order not exists")
    void shouldThrowExceptionWhenGetOrderByIdAndOrderNotExists() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ORDER_NOT_FOUND_MESSAGE + 999);
    }

    @Test
    @DisplayName("Should get order by id when order exists")
    void shouldGetOrderByIdWhenOrderExists() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        orderService.createOrder(orderRequest);

        List<OrderResponseDTO> allOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);
        assertThat(allOrders).isNotEmpty();

        OrderResponseDTO existingOrder = allOrders.get(0);

        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        Long orderId = orderRepository.findAll().get(0).getId();
        OrderResponseDTO foundOrder = orderService.getOrderById(orderId);

        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.userInfoDto().email()).isEqualTo(USER_EMAIL);
        assertThat(foundOrder.status()).isEqualTo(OrderStatus.PENDING);

        verify(getRequestedFor(urlPathMatching("/api/v1/users/email/.*")));
    }

    @Test
    @DisplayName("Should get orders by ids when orders exist")
    void shouldGetOrdersByIdsWhenOrdersExist() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest1 = new OrderRequestDTO(123L, "test@example.com",
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        OrderRequestDTO orderRequest2 = new OrderRequestDTO(124L, "test@example.com",
                List.of(new OrderItemDTO(
                        new ItemDTO(2L, "Mouse", new BigDecimal(25)), 2L)));

        orderService.createOrder(orderRequest1);
        orderService.createOrder(orderRequest2);

        List<Long> orderIds = orderRepository.findAll().stream()
                .map(order -> order.getId())
                .collect(Collectors.toList());

        assertThat(orderIds).hasSize(2);

        List<OrderResponseDTO> foundOrders = orderService.getOrdersByIds(orderIds);

        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders).extracting(OrderResponseDTO::status)
                .containsOnly(OrderStatus.PENDING);

        assertThat(foundOrders).allMatch(order -> order.userInfoDto() != null);
    }

    @Test
    @DisplayName("Should get orders by status when orders exist")
    void shouldGetOrdersByStatusWhenOrdersExist() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest1 = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        OrderRequestDTO orderRequest2 = new OrderRequestDTO(124L, "test2@example.com",
                List.of(new OrderItemDTO(
                        new ItemDTO(2L, "Mouse", new BigDecimal(25)), 2L)));

        orderService.createOrder(orderRequest1);
        orderService.createOrder(orderRequest2);

        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        stubFor(get(urlPathEqualTo("/api/v1/users/email/test2@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "124",
                                    "email": "test2@example.com",
                                    "name": "Jane",
                                    "surname": "Smith",
                                    "birthdate": "2000-01-01"
                                }
                                """)));

        List<OrderResponseDTO> foundOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertThat(foundOrders).isNotEmpty();
        assertThat(foundOrders).allMatch(order -> order.status() == OrderStatus.PENDING);
        assertThat(foundOrders).extracting(OrderResponseDTO::userId)
                .contains(123L, 124L);
    }

    @Test
    @DisplayName("Should update order when order exists")
    void shouldUpdateOrderWhenOrderExists() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        orderService.createOrder(orderRequest);

        Long orderId = orderRepository.findAll().stream()
                .findFirst()
                .map(order -> order.getId())
                .orElseThrow(() -> new AssertionError("Order not found in database"));

        OrderRequestDTO updateRequest = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(2L, "Updated Laptop", new BigDecimal(80)), 2L)));

        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderResponseDTO updatedOrder = orderService.updateOrderById(orderId, updateRequest);

        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.userInfoDto().email()).isEqualTo(USER_EMAIL);
        assertThat(updatedOrder.status()).isEqualTo(OrderStatus.PENDING);

        OrderResponseDTO foundAfterUpdate = orderService.getOrderById(orderId);
        assertThat(foundAfterUpdate).isNotNull();
        assertThat(foundAfterUpdate.userInfoDto().email()).isEqualTo(USER_EMAIL);
    }

    @Test
    @DisplayName("Should delete order when order exists")
    void shouldDeleteOrderWhenOrderExists() {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "test@example.com",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """)));

        OrderRequestDTO orderRequest = new OrderRequestDTO(123L, USER_EMAIL,
                List.of(new OrderItemDTO(
                        new ItemDTO(1L, "Laptop", new BigDecimal(70)), 1L)));

        orderService.createOrder(orderRequest);

        Long orderId = orderRepository.findAll().stream()
                .findFirst()
                .map(order -> order.getId())
                .orElseThrow(() -> new AssertionError("Order not found in database"));

        assertThatNoException().isThrownBy(() -> orderService.deleteOrderById(orderId));

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ORDER_NOT_FOUND_MESSAGE + orderId);

        List<Order> remainingOrders = orderRepository.findAll();
        assertThat(remainingOrders).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when delete order not exists")
    void shouldThrowExceptionWhenDeleteOrderNotExists() {
        assertThatThrownBy(() -> orderService.deleteOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(ORDER_NOT_FOUND_MESSAGE + 999);
    }
}