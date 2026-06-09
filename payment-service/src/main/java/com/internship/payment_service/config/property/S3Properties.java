package com.internship.payment_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.s3")
@Getter
@Setter
public class S3Properties {

    private String receiptBucketName;
}
