package com.internship.payment_service.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
public class SqsTemplateConfig {

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configureDefaultConverter(converter ->
                        converter.setPayloadTypeHeaderValueFunction(message -> null))
                .build();
    }
}
