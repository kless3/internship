package com.internship.user_service.service.impl;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.mapper.CardInfoMapper;
import com.internship.user_service.model.CardInfo;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardInfoRepository;
import com.internship.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardInfoRepository cardInfoRepository;

    @Mock
    private UserRepository userRepository;

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

        cardInfoRequestDTO = new CardInfoRequestDTO();
        cardInfoRequestDTO.setNumber("1234567890123456");

        cardInfoResponseDTO = new CardInfoResponseDTO();
        cardInfoResponseDTO.setId(1L);
        cardInfoResponseDTO.setNumber("1234567890123456");
    }

    @Test
    void createCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardInfoRepository.existsByNumber(cardInfoRequestDTO.getNumber())).thenReturn(false);
        when(cardInfoMapper.toEntity(cardInfoRequestDTO)).thenReturn(cardInfo);
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.createCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.getNumber(), result.getNumber());
        verify(cardInfoRepository, times(1)).save(cardInfo);
    }

    @Test
    void createCard_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                cardService.createCard(1L, cardInfoRequestDTO));
    }

    @Test
    void createCard_CardNumberExists_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardInfoRepository.existsByNumber(cardInfoRequestDTO.getNumber())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                cardService.createCard(1L, cardInfoRequestDTO));
    }

    @Test
    void getCardById_Success() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.getId(), result.getId());
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                cardService.getCardById(1L));
    }

    @Test
    void getCardByNumber_Success() {
        when(cardInfoRepository.findByNumber("1234567890123456")).thenReturn(Optional.of(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.getCardByNumber("1234567890123456");

        assertNotNull(result);
        assertEquals(cardInfoResponseDTO.getNumber(), result.getNumber());
    }

    @Test
    void getCardsByUserId_Success() {
        when(cardInfoRepository.findByUserId(1L)).thenReturn(Arrays.asList(cardInfo));
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getCardsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(cardInfoResponseDTO.getId(), result.get(0).getId());
    }

    @Test
    void getCardsByIds_Success() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<CardInfo> cards = Arrays.asList(cardInfo);
        when(cardInfoRepository.findByIdIn(ids)).thenReturn(cards);
        when(cardInfoMapper.toDTO(any(CardInfo.class))).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getCardsByIds(ids);

        assertEquals(1, result.size());
    }

    @Test
    void getAllCards_Success() {
        List<CardInfo> cards = Arrays.asList(cardInfo);
        when(cardInfoRepository.findAll()).thenReturn(cards);
        when(cardInfoMapper.toDTO(any(CardInfo.class))).thenReturn(cardInfoResponseDTO);

        List<CardInfoResponseDTO> result = cardService.getAllCards();

        assertEquals(1, result.size());
    }

    @Test
    void updateCard_Success() {
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        // Same number - should not check for existence
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.updateCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        verify(cardInfoMapper, times(1)).updateEntityFromDTO(cardInfoRequestDTO, cardInfo);
        // Should NOT check for number existence when number hasn't changed
        verify(cardInfoRepository, never()).existsByNumber(any());
    }

    @Test
    void updateCard_NumberExists_ThrowsException() {
        CardInfoRequestDTO updateRequest = new CardInfoRequestDTO();
        updateRequest.setNumber("9999999999999999"); // Different number

        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.existsByNumber("9999999999999999")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                cardService.updateCard(1L, updateRequest));
    }

    @Test
    void updateCard_SameNumber_Success() {
        // Same number as current card - should not throw exception
        when(cardInfoRepository.findById(1L)).thenReturn(Optional.of(cardInfo));
        when(cardInfoRepository.save(cardInfo)).thenReturn(cardInfo);
        when(cardInfoMapper.toDTO(cardInfo)).thenReturn(cardInfoResponseDTO);

        CardInfoResponseDTO result = cardService.updateCard(1L, cardInfoRequestDTO);

        assertNotNull(result);
        // Should NOT check for number existence when number is the same
        verify(cardInfoRepository, never()).existsByNumber(any());
    }

    @Test
    void deleteCard_Success() {
        when(cardInfoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cardInfoRepository).deleteById(1L);

        assertDoesNotThrow(() -> cardService.deleteCard(1L));
        verify(cardInfoRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCard_NotFound_ThrowsException() {
        when(cardInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                cardService.deleteCard(1L));
    }

    @Test
    void cardExists_ReturnsTrue() {
        when(cardInfoRepository.existsById(1L)).thenReturn(true);

        assertTrue(cardService.cardExists(1L));
    }

    @Test
    void cardExists_ReturnsFalse() {
        when(cardInfoRepository.existsById(1L)).thenReturn(false);

        assertFalse(cardService.cardExists(1L));
    }

    @Test
    void cardNumberExists_ReturnsTrue() {
        when(cardInfoRepository.existsByNumber("1234567890123456")).thenReturn(true);

        assertTrue(cardService.cardNumberExists("1234567890123456"));
    }
}