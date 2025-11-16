package com.internship.user_service.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> postgreSQLContainer;
    protected static final GenericContainer<?> redisContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("internship_test")
                .withUsername("postgres")
                .withPassword("postgres")
                .withReuse(false);
        postgreSQLContainer.start();

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(false);
        redisContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("spring.data.redis.timeout", () -> "10000ms");

        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
    }
}