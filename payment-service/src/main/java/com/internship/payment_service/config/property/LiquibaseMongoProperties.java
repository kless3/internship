package com.internship.payment_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.liquibase-mongodb")
public class LiquibaseMongoProperties {
    private boolean enabled;
    private String changeLog;
}