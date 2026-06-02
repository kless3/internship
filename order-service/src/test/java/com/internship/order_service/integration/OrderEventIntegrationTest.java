package com.internship.order_service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.request.UpdateShippingAddressRequestDto;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.OrderItemDto;
import com.internship.order_service.dto.response.OrderPriceResponseDto;
import com.internship.order_service.model.Order;
import com.internship.order_service.model.OrderEvent;
import com.internship.order_service.model.enums.OrderEventStatus;
import com.internship.order_service.repository.OrderEventRepository;
import com.internship.order_service.repository.OrderRepository;
import com.internship.order_service.service.OrderPriceService;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Order Event Payload Integration Tests")
class OrderEventIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "payload@example.com";
    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @Autowired
    private OrderLifecycleServiceImpl orderLifecycleService;

    @Autowired
    private OrderProcessingServiceImpl orderProcessingService;

    @Autowired
    private OrderPriceService orderPriceService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventRepository orderEventRepository;

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        startWireMock();
        String wireMockUrl = "http://localhost:" + wireMockServer.port();
        registry.add("user.service.url", () -> wireMockUrl);
    }

    @BeforeAll
    static void beforeAll() {
        startWireMock();
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        orderEventRepository.deleteAll();
        orderRepository.deleteAll();
        wireMockServer.resetAll();
        WireMock.configureFor("localhost", wireMockServer.port());
        stubUser(USER_EMAIL);
    }

    @Test
    void createOrder_shouldPersistCreatedEventPayload() {
        Long orderId = createOrder("Initial address");

        OrderEvent createdEvent = findEvent(orderId, OrderEventStatus.CREATED);

        assertThat(createdEvent.getPayload()).containsEntry("shippingAddress", "Initial address");
    }

    @Test
    void updateShippingAddressAndRestore_shouldReplayAddressFromEventPayload() throws InterruptedException {
        Long orderId = createOrder("Address A");
        OrderEvent createdEvent = findEvent(orderId, OrderEventStatus.CREATED);

        Thread.sleep(25);
        orderLifecycleService.updateShippingAddress(orderId, new UpdateShippingAddressRequestDto("Address B"));

        OrderEvent updatedEvent = findEvent(orderId, OrderEventStatus.SHIPPING_ADDRESS_UPDATED);
        assertThat(updatedEvent.getPayload()).containsEntry("shippingAddress", "Address B");

        orderProcessingService.restoreOrderStatusAt(orderId, createdEvent.getEventTimestamp());

        Order restoredOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order not found in database"));
        assertThat(restoredOrder.getShippingAddress()).isEqualTo("Address A");
    }

    @Test
    void applyDiscountAndGetOrderPriceAt_shouldUseDiscountPayload() throws InterruptedException {
        Long orderId = createOrder("Discount address");

        Thread.sleep(25);
        orderProcessingService.applyDiscount(orderId, new BigDecimal("15.00"));

        OrderEvent discountEvent = findEvent(orderId, OrderEventStatus.DISCOUNT_APPLIED);
        BigDecimal payloadDiscount = new BigDecimal(discountEvent.getPayload().get("discountPercent").toString());
        assertThat(payloadDiscount).isEqualByComparingTo("15.00");

        OrderPriceResponseDto price = orderPriceService.getOrderPriceAt(orderId, discountEvent.getEventTimestamp());

        assertThat(price.subtotal()).isEqualByComparingTo("1500.00");
        assertThat(price.discountPercent()).isEqualByComparingTo("15.00");
        assertThat(price.total()).isEqualByComparingTo("1275.00");
    }

    private Long createOrder(String shippingAddress) {
        OrderRequestDto orderRequest = new OrderRequestDto(
                123L,
                USER_EMAIL,
                shippingAddress,
                List.of(new OrderItemDto(new ItemDto(1L, "Laptop", new BigDecimal("1500.00")), 1L))
        );

        orderLifecycleService.createOrder(orderRequest);

        return orderRepository.findAll().stream()
                .findFirst()
                .map(Order::getId)
                .orElseThrow(() -> new AssertionError("Order not found in database"));
    }

    private OrderEvent findEvent(Long orderId, OrderEventStatus status) {
        return orderEventRepository.findAllByOrderIdOrderByEventTimestampAsc(orderId)
                .stream()
                .filter(event -> event.getStatus() == status)
                .findFirst()
                .orElseThrow(() -> new AssertionError(status + " event not found"));
    }

    private void stubUser(String email) {
        stubFor(get(urlPathMatching("/api/v1/users/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "123",
                                    "email": "%s",
                                    "name": "John",
                                    "surname": "Doe",
                                    "birthdate": "1999-05-05"
                                }
                                """.formatted(email))));
    }

    private static void startWireMock() {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            WireMock.configureFor("localhost", wireMockServer.port());
        }
    }
}
