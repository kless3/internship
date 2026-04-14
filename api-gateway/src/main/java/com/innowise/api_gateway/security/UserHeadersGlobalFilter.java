package com.innowise.api_gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserHeadersGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(principal -> {
                    if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
                        return chain.filter(exchange);
                    }

                    String username = resolveUsername(jwtAuth);

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", username)
                            .header("X-User-Name", username)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String resolveUsername(JwtAuthenticationToken jwtAuth) {
        String email = jwtAuth.getToken().getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        String preferredUsername = jwtAuth.getToken().getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        return jwtAuth.getToken().getSubject();
    }
}
