package com.internship.user_service.service;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;

import java.util.List;

public interface CardInfoService {

    CardInfoResponseDTO createCard(Long userId, CardInfoRequestDTO cardInfoRequestDTO);

    CardInfoResponseDTO getCardById(Long id);

    CardInfoResponseDTO getCardByNumber(String number);

    List<CardInfoResponseDTO> getCardsByIds(List<Long> ids);

    List<CardInfoResponseDTO> getCardsByUserId(Long userId);

    List<CardInfoResponseDTO> getAllCards();

    CardInfoResponseDTO updateCard(Long id, CardInfoRequestDTO cardInfoRequestDTO);

    void deleteCard(Long id);

    boolean cardExists(Long id);

    boolean cardNumberExists(String number);
}
