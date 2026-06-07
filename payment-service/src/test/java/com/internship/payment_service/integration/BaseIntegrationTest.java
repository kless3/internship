package com.internship.payment_service.integration;

import com.internship.payment_service.model.Payment;
import com.internship.payment_service.service.PaymentReceiptService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
public abstract class BaseIntegrationTest {

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withReuse(true);

    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true);

    static {
        mongoDBContainer.start();
        kafkaContainer.start();
    }

    @MockBean
    private PaymentReceiptService paymentReceiptService;

    @org.junit.jupiter.api.BeforeEach
    void configureReceiptService() {
        when(paymentReceiptService.createReceipt(any(Payment.class)))
                .thenReturn("receipts/test/payment-test.pdf");
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("external.api.url", () -> "http://localhost:8089/integers");
        registry.add("app.messaging.transport", () -> "kafka");
    }
}
