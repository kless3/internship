package com.innowise.api_gateway.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
public class ServiceProperties {

    private String authService;
    private String orderService;
    private String userService;
    private String paymentService;

}
