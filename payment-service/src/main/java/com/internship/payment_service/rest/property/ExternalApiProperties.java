package com.internship.payment_service.rest.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Getter
@Setter
public class ExternalApiProperties {

    private String url;

}
