package com.internship.user_service.unit;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.mapper.CardInfoMapper;
import com.internship.user_service.model.CardInfo;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardInfoRepository;
import com.internship.user_service.service.UserService;
import com.internship.user_service.service.impl.CardServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Class for CardServiceImpl")
class CardServiceImplTest {

    @Mock
    private CardInfoRepository cardInfoRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardInfoMapper cardInfoMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardInfo cardInfo;
    private CardInfoRequestDTO cardInfoRequestDTO;
    private CardInfoResponseDTO cardInfoResponseDTO;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        cardInfo = new CardInfo();
        cardInfo.setId(1L);
        cardInfo.setNumber("1234567890123456");
        cardInfo.setUser(user);

        cardInfoRequestDTO = new CardInfoRequestDTO(
                "1234567890123456",
                "CARD USER",
                LocalDate.of(2025, 12, 1)
        );

        cardInfoResponseDTO = new CardInfoResponseDTO(
                1L,
                "1234567890123456",
                "CARD USER",
                LocalDate.of(2025, 12, 1),
                1L
        );
    }

    @Test
    @DisplayName("Create card - success")
    void createCard_Success() {
        when(userService.getUserEntityById(1L)).thenReturn(user);
        when(cardInfoRepository.existsByNumber(cardInfoRequestDTO.number())).thenReturn(false);
        when(cardInfoMapper.toEntity(cardInfoRequestDTO)).thenReturn(cardInfo);
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.createCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.number(), result.number());
        verify(cardInfoRepository, times(1)).save(cardInfo);
    }

    @Test
    @DisplayName("Create card - card number exists, should throw exception")
    void createCard_CardNumberExists_ThrowsException() {
        when(userService.getUserEntityById(1L)).thenReturn(user);
        when(cardInfoRepository.existsByNumber(cardInfoRequestDTO.number())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                cardService.createCard(1L, cardInfoRequestDTO));
    }

    @Test
    @DisplayName("Get card by ID - success")
    void getCardById_Success() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.id(), result.id());
    }

    @Test
    @DisplayName("Get card by ID - not found, should throw exception")
    void getCardById_NotFound_ThrowsException() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.getCardById(1L));
    }

    @Test
    @DisplayName("Get card by number - success")
    void getCardByNumber_Success() {
        when(cardInfoRepository.findByNumber("1234567890123456")).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.getCardByNumber("1234567890123456");

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.number(), result.number());
    }

    @Test
    @DisplayName("Get cards by user ID - success")
    void getCardsByUserId_Success() {
        when(cardInfoRepository.findByUserId(1L)).thenReturn(Arrays.asList(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getCardsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(cardInfoResponseDTO.id(), result.get(0).id());
    }

    @Test
    @DisplayName("Get cards by IDs - success")
    void getCardsByIds_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<CardInfo> cards = Arrays.asList(cardInfo);
        when(cardInfoRepository.findByIdIn(ids)).thenReturn(cards);
        when(cardInfoMapper.toDTO(any(CardInfo.class))).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getCardsByIds(ids);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Get all cards - success")
    void getAllCards_Success() {
        List<CardInfo> cards = Arrays.asList(cardInfo);
        when(cardInfoRepository.findAll()).thenReturn(cards);
        when(cardInfoMapper.toDTO(any(CardInfo.class))).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getAllCards();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Update card - success")
    void updateCard_Success() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.updateCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        verify(cardInfoMapper, times(1)).updateEntityFromDTO(cardInfoRequestDTO, cardInfo);
        verify(cardInfoRepository, never()).existsByNumber(any());
    }

    @Test
    @DisplayName("Update card - number exists, should throw exception")
    void updateCard_NumberExists_ThrowsException() {
        CardInfoRequestDTO updateRequest = new CardInfoRequestDTO(
                "9999999999999999",
                "UPDATED USER",
                LocalDate.of(2026, 10, 1)
        );

        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.existsByNumber("9999999999999999")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                cardService.updateCard(1L, updateRequest));
    }

    @Test
    @DisplayName("Update card - same number, success")
    void updateCard_SameNumber_Success() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.updateCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        verify(cardInfoRepository, never()).existsByNumber(any());
    }

    @Test
    @DisplayName("Delete card - success")
    void deleteCard_Success() {
        when(cardInfoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cardInfoRepository).deleteById(1L);

        assertDoesNotThrow(() -> cardService.deleteCard(1L));
        verify(cardInfoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete card - not found, should throw exception")
    void deleteCard_NotFound_ThrowsException() {
        when(cardInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                cardService.deleteCard(1L));
    }

    @Test
    @DisplayName("Card exists - returns true")
    void cardExists_ReturnsTrue() {
        when(cardInfoRepository.existsById(1L)).thenReturn(true);

        assertTrue(cardService.cardExists(1L));
    }

    @Test
    @DisplayName("Card exists - returns false")
    void cardExists_ReturnsFalse() {
        when(cardInfoRepository.existsById(1L)).thenReturn(false);

        assertFalse(cardService.cardExists(1L));
    }

    @Test
    @DisplayName("Card number exists - returns true")
    void cardNumberExists_ReturnsTrue() {
        when(cardInfoRepository.existsByNumber("1234567890123456")).thenReturn(true);

        assertTrue(cardService.cardNumberExists("1234567890123456"));
    }
}