package com.internship.payment_service.config;

import com.internship.payment_service.config.property.SqsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "sqs")
@RequiredArgsConstructor
public class SqsConfig {

    private final SqsProperties sqsProperties;

    @Bean
    public SqsClient sqsClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                sqsProperties.getAccessKey(),
                sqsProperties.getSecretKey()
        );

        return SqsClient.builder()
                .endpointOverride(URI.create(sqsProperties.getEndpoint()))
                .region(Region.of(sqsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
