package com.internship.user_service.integration;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.service.CardInfoService;
import com.internship.user_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Integration Tests for CardServiceImpl")
class CardServiceIntegrationTest {

    @Autowired
    private CardInfoService cardInfoService;

    @Autowired
    private UserService userService;

    private UserResponseDTO testUser;
    private CardInfoRequestDTO testCardRequest;

    @Container
    private static GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(false);

    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("internship_test2")
            .withUsername("postgres")
            .withPassword("postgres")
            .withReuse(false);

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
    }

    @BeforeEach
    void setUp() {
        UserRequestDTO userRequest = new UserRequestDTO();
        userRequest.setName("Card");
        userRequest.setSurname("User");
        userRequest.setEmail("card.user@example.com");
        testUser = userService.createUser(userRequest);

        testCardRequest = new CardInfoRequestDTO();
        testCardRequest.setNumber("4111111111111111");
        testCardRequest.setHolder("CARD USER");
        testCardRequest.setExpirationDate(LocalDate.of(2025, 12, 1));
    }

    @Test
    @DisplayName("Create card - should create card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createCard_ShouldCreateCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.getId(), testCardRequest);

        assertNotNull(createdCard.getId());
        assertEquals(testCardRequest.getNumber(), createdCard.getNumber());
        assertEquals(testCardRequest.getHolder(), createdCard.getHolder());
        assertEquals(testUser.getId(), createdCard.getUserId());
    }

    @Test
    @DisplayName("Create card with existing number - should throw exception")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createCard_WithExistingNumber_ShouldThrowException() {
        cardInfoService.createCard(testUser.getId(), testCardRequest);

        assertThrows(IllegalArgumentException.class, () -> {
            cardInfoService.createCard(testUser.getId(), testCardRequest);
        });
    }

    @Test
    @DisplayName("Create card with non-existing user - should throw exception")
    void createCard_WithNonExistingUser_ShouldThrowException() {
        assertThrows(EntityNotFoundException.class, () -> {
            cardInfoService.createCard(999L, testCardRequest);
        });
    }

    @Test
    @DisplayName("Get card by ID - should return card")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardById_ShouldReturnCard() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.getId(), testCardRequest);

        CardInfoResponseDTO foundCard = cardInfoService.getCardById(createdCard.getId());

        assertNotNull(foundCard);
        assertEquals(createdCard.getId(), foundCard.getId());
        assertEquals(createdCard.getNumber(), foundCard.getNumber());
    }

    @Test
    @DisplayName("Get card by number - should return card")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardByNumber_ShouldReturnCard() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.getId(), testCardRequest);

        CardInfoResponseDTO foundCard = cardInfoService.getCardByNumber(createdCard.getNumber());

        assertNotNull(foundCard);
        assertEquals(createdCard.getId(), foundCard.getId());
        assertEquals(createdCard.getNumber(), foundCard.getNumber());
    }

    @Test
    @DisplayName("Get cards by user ID - should return user cards")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardsByUserId_ShouldReturnUserCards() {
        cardInfoService.createCard(testUser.getId(), testCardRequest);

        CardInfoRequestDTO anotherCard = new CardInfoRequestDTO();
        anotherCard.setNumber("4222222222222222");
        anotherCard.setHolder("CARD USER");
        anotherCard.setExpirationDate(LocalDate.of(2024, 11, 1));

        cardInfoService.createCard(testUser.getId(), anotherCard);

        List<CardInfoResponseDTO> userCards = cardInfoService.getCardsByUserId(testUser.getId());

        assertEquals(2, userCards.size());
    }

    @Test
    @DisplayName("Update card - should update card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCard_ShouldUpdateCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.getId(), testCardRequest);

        CardInfoRequestDTO updateRequest = new CardInfoRequestDTO();
        updateRequest.setNumber("4333333333333333");
        updateRequest.setHolder("UPDATED USER");
        updateRequest.setExpirationDate(LocalDate.of(2026, 10, 1));


        CardInfoResponseDTO updatedCard = cardInfoService.updateCard(createdCard.getId(), updateRequest);

        assertEquals(createdCard.getId(), updatedCard.getId());
        assertEquals("4333333333333333", updatedCard.getNumber());
        assertEquals("UPDATED USER", updatedCard.getHolder());
    }

    @Test
    @DisplayName("Delete card - should delete card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteCard_ShouldDeleteCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.getId(), testCardRequest);

        cardInfoService.deleteCard(createdCard.getId());

        assertThrows(EntityNotFoundException.class, () -> {
            cardInfoService.getCardById(createdCard.getId());
        });
        assertFalse(cardInfoService.cardExists(createdCard.getId()));
    }

    @Test
    @DisplayName("Get cards by IDs - should return multiple cards")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardsByIds_ShouldReturnMultipleCards() {
        CardInfoResponseDTO card1 = cardInfoService.createCard(testUser.getId(), testCardRequest);

        CardInfoRequestDTO card2Request = new CardInfoRequestDTO();
        card2Request.setNumber("4222222222222222");
        card2Request.setHolder("SECOND CARD");
        card2Request.setExpirationDate(LocalDate.of(2024, 11, 1));

        CardInfoResponseDTO card2 = cardInfoService.createCard(testUser.getId(), card2Request);

        List<CardInfoResponseDTO> cards = cardInfoService.getCardsByIds(List.of(card1.getId(), card2.getId()));

        assertEquals(2, cards.size());
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals(card1.getId())));
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals(card2.getId())));
    }
}

