package com.innowise.api_gateway.config;

import com.innowise.api_gateway.security.JwtGlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final JwtGlobalFilter jwtFilter;

    public GatewayConfig(JwtGlobalFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .uri("http://localhost:8082"))
                .route("user-service-public", r -> r
                        .path("/api/v1/users")
                        .uri("http://localhost:8080"))
                .route("user-service-secured", r -> r
                        .path("/api/v1/users/**")
                        .uri("http://localhost:8080"))
                .build();
    }
}