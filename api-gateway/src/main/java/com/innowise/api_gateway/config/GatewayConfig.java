package com.innowise.api_gateway.config;

import com.innowise.api_gateway.property.ServiceProperties;
import com.innowise.api_gateway.security.JwtGlobalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private static final String AUTH_SERVICE_ROUTE = "auth-service";
    private static final String ORDER_SERVICE_ROUTE = "order-service";
    private static final String PAYMENT_SERVICE_ROUTE = "payment-service";
    private static final String USER_SERVICE_PUBLIC_ROUTE = "user-service-public";
    private static final String USER_SERVICE_SECURED_ROUTE = "user-service-secured";
    private static final String AUTH_SERVICE_PATH = "/api/v1/auth/**";
    private static final String ORDER_SERVICE_PATH = "/api/v1/orders/**";
    private static final String PAYMENT_SERVICE_PATH = "/api/v1/payments/**";
    private static final String USER_SERVICE_PUBLIC_PATH = "/api/v1/users";
    private static final String USER_SERVICE_SECURED_PATH = "/api/v1/users/**";

    private final JwtGlobalFilter jwtFilter;
    private final ServiceProperties serviceProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(AUTH_SERVICE_ROUTE, r -> r
                        .path(AUTH_SERVICE_PATH)
                        .uri(serviceProperties.getAuthService()))
                .route(ORDER_SERVICE_ROUTE, r -> r
                        .path(ORDER_SERVICE_PATH)
                        .filters(f -> f.filter(jwtFilter))
                        .uri(serviceProperties.getOrderService()))
                .route(USER_SERVICE_PUBLIC_ROUTE, r -> r
                        .path(USER_SERVICE_PUBLIC_PATH)
                        .uri(serviceProperties.getUserService()))
                .route(USER_SERVICE_SECURED_ROUTE, r -> r
                        .path(USER_SERVICE_SECURED_PATH)
                        .filters(f -> f.filter(jwtFilter))
                        .uri(serviceProperties.getUserService()))
                .route(PAYMENT_SERVICE_ROUTE, r -> r
                        .path(PAYMENT_SERVICE_PATH)
                        .filters(f -> f.filter(jwtFilter))
                        .uri(serviceProperties.getPaymentService()))
                .build();
    }
}