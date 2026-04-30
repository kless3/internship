package com.internship.order_service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.OrderEventResponseDto;
import com.internship.order_service.dto.response.OrderItemDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.exception.OrderValidationException;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.enums.OrderStatus;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.impl.OrderLifecycleServiceImpl;
import com.internship.order_service.service.impl.OrderProcessingServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@DisplayName("Order Processing Integration Tests")
class OrderProcessingServiceIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "test@example.com";

    @Autowired
    private OrderLifecycleServiceImpl orderLifecycleService;

    @Autowired
    private OrderProcessingServiceImpl orderProcessingService;

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
        stubUser(USER_EMAIL, "123");
    }

    @Test
    void payOrder_shouldSwitchStatusToProcessing() {
        Long orderId = createOrder(USER_EMAIL);

        OrderResponseDto result = orderProcessingService.payOrder(orderId);

        assertThat(result.status()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void getOrderHistory_shouldReturnCreatedEventAfterCreation() {
        Long orderId = createOrder(USER_EMAIL);

        List<OrderEventResponseDto> history = orderProcessingService.getOrderHistory(orderId);

        assertThat(history).isNotEmpty();
        assertThat(history.get(0).status().name()).isEqualTo("CREATED");
    }

    @Test
    void restoreOrderStatusAt_shouldThrowForFutureTimestamp() {
        Long orderId = createOrder(USER_EMAIL);

        assertThatThrownBy(() -> orderProcessingService.restoreOrderStatusAt(orderId, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(OrderValidationException.class)
                .hasMessageContaining("Restore timestamp cannot be in the future");
    }

    private Long createOrder(String email) {
        OrderRequestDto orderRequest = new OrderRequestDto(123L, email, "Test address",
                List.of(new OrderItemDto(new ItemDto(1L, "Laptop", new BigDecimal("70")), 1L)));

        orderLifecycleService.createOrder(orderRequest);

        Order order = orderRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order not found in database"));

        return order.getId();
    }

    private void stubUser(String email, String id) {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "%s",
                                    "email": "%s",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """.formatted(id, email))));
    }
}
