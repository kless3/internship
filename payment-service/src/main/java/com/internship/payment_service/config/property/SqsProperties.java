package com.internship.payment_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.sqs")
@Getter
@Setter
public class SqsProperties {

    private String orderCreatedQueueName;
    private String paymentCreatedQueueName;
}
