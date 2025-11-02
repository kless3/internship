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

    private final JwtGlobalFilter jwtFilter;
    private final ServiceProperties serviceProperties;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri(serviceProperties.getAuthService()))
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .uri(serviceProperties.getOrderService()))
                .route("user-service-public", r -> r
                        .path("/api/v1/users")
                        .uri(serviceProperties.getUserService()))
                .route("user-service-secured", r -> r
                        .path("/api/v1/users/**")
                        .uri(serviceProperties.getUserService()))
                .build();
    }
}