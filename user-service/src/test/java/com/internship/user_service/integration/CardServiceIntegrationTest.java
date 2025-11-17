package com.internship.user_service.integration;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.service.CardInfoService;
import com.internship.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Integration Tests for CardServiceImpl")
class CardServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CardInfoService cardInfoService;

    @Autowired
    private UserService userService;

    private UserResponseDTO testUser;
    private CardInfoRequestDTO testCardRequest;

    @BeforeEach
    void setUp() {
        UserRequestDTO userRequest = new UserRequestDTO(
                "Card",
                "User",
                LocalDate.of(1990, 1, 1),
                "card.user@example.com"
        );
        testUser = userService.createUser(userRequest);

        testCardRequest = new CardInfoRequestDTO(
                "4111111111111111",
                "CARD USER",
                LocalDate.of(2025, 12, 1)
        );
    }

    @Test
    @DisplayName("Create card - should create card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createCard_ShouldCreateCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.id(), testCardRequest);

        assertNotNull(createdCard.id());
        assertEquals(testCardRequest.number(), createdCard.number());
        assertEquals(testCardRequest.holder(), createdCard.holder());
        assertEquals(testUser.id(), createdCard.userId());
    }

    @Test
    @DisplayName("Create card with existing number - should throw exception")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createCard_WithExistingNumber_ShouldThrowException() {
        cardInfoService.createCard(testUser.id(), testCardRequest);

        assertThrows(DuplicateResourceException.class, () -> {
            cardInfoService.createCard(testUser.id(), testCardRequest);
        });
    }

    @Test
    @DisplayName("Create card with non-existing user - should throw exception")
    void createCard_WithNonExistingUser_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            cardInfoService.createCard(999L, testCardRequest);
        });
    }

    @Test
    @DisplayName("Get card by ID - should return card")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardById_ShouldReturnCard() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.id(), testCardRequest);

        CardInfoResponseDTO foundCard = cardInfoService.getCardById(createdCard.id());

        assertNotNull(foundCard);
        assertEquals(createdCard.id(), foundCard.id());
        assertEquals(createdCard.number(), foundCard.number());
    }

    @Test
    @DisplayName("Get card by number - should return card")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardByNumber_ShouldReturnCard() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.id(), testCardRequest);

        CardInfoResponseDTO foundCard = cardInfoService.getCardByNumber(createdCard.number());

        assertNotNull(foundCard);
        assertEquals(createdCard.id(), foundCard.id());
        assertEquals(createdCard.number(), foundCard.number());
    }

    @Test
    @DisplayName("Get cards by user ID - should return user cards")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardsByUserId_ShouldReturnUserCards() {
        cardInfoService.createCard(testUser.id(), testCardRequest);

        CardInfoRequestDTO anotherCard = new CardInfoRequestDTO(
                "4222222222222222",
                "CARD USER",
                LocalDate.of(2024, 11, 1)
        );

        cardInfoService.createCard(testUser.id(), anotherCard);

        List<CardInfoResponseDTO> userCards = cardInfoService.getCardsByUserId(testUser.id());

        assertEquals(2, userCards.size());
    }

    @Test
    @DisplayName("Update card - should update card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCard_ShouldUpdateCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.id(), testCardRequest);

        CardInfoRequestDTO updateRequest = new CardInfoRequestDTO(
                "4333333333333333",
                "UPDATED USER",
                LocalDate.of(2026, 10, 1)
        );

        CardInfoResponseDTO updatedCard = cardInfoService.updateCard(createdCard.id(), updateRequest);

        assertEquals(createdCard.id(), updatedCard.id());
        assertEquals("4333333333333333", updatedCard.number());
        assertEquals("UPDATED USER", updatedCard.holder());
    }

    @Test
    @DisplayName("Delete card - should delete card successfully")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteCard_ShouldDeleteCardSuccessfully() {
        CardInfoResponseDTO createdCard = cardInfoService.createCard(testUser.id(), testCardRequest);

        cardInfoService.deleteCard(createdCard.id());

        assertThrows(ResourceNotFoundException.class, () -> {
            cardInfoService.getCardById(createdCard.id());
        });
        assertFalse(cardInfoService.cardExists(createdCard.id()));
    }

    @Test
    @DisplayName("Get cards by IDs - should return multiple cards")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCardsByIds_ShouldReturnMultipleCards() {
        CardInfoResponseDTO card1 = cardInfoService.createCard(testUser.id(), testCardRequest);

        CardInfoRequestDTO card2Request = new CardInfoRequestDTO(
                "4222222222222222",
                "SECOND CARD",
                LocalDate.of(2024, 11, 1)
        );

        CardInfoResponseDTO card2 = cardInfoService.createCard(testUser.id(), card2Request);

        List<CardInfoResponseDTO> cards = cardInfoService.getCardsByIds(List.of(card1.id(), card2.id()));

        assertEquals(2, cards.size());
        assertTrue(cards.stream().anyMatch(c -> c.id().equals(card1.id())));
        assertTrue(cards.stream().anyMatch(c -> c.id().equals(card2.id())));
    }
}