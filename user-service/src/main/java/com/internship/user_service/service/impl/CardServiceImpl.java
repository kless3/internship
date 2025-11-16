package com.internship.user_service.service.impl;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.mapper.CardInfoMapper;
import com.internship.user_service.model.CardInfo;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardInfoRepository;
import com.internship.user_service.service.CardInfoService;
import com.internship.user_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig(cacheNames = "cards")
@RequiredArgsConstructor
public class CardServiceImpl implements CardInfoService {

    private static final String CARD_NOT_FOUND_WITH_ID = "Card not found with id: ";
    private static final String CARD_NOT_FOUND_WITH_NUMBER = "Card not found with number: ";
    private static final String CARD_ALREADY_EXISTS_WITH_NUMBER = "Card with number %s already exists";

    private final CardInfoRepository cardInfoRepository;
    private final UserService userService;
    private final CardInfoMapper cardInfoMapper;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cards", allEntries = true),
            @CacheEvict(cacheNames = "users", key = "'all'")
    })
    public CardInfoResponseDTO createCard(Long userId, CardInfoRequestDTO cardInfoRequestDTO) {

        User user = userService.getUserEntityById(userId);

        if (cardInfoRepository.existsByNumber(cardInfoRequestDTO.number())) {
            throw new DuplicateResourceException(String.format(CARD_ALREADY_EXISTS_WITH_NUMBER, cardInfoRequestDTO.number()));
        }

        CardInfo cardInfo = cardInfoMapper.toEntity(cardInfoRequestDTO);
        cardInfo.setUser(user);

        CardInfo savedCard = cardInfoRepository.save(cardInfo);

        return cardInfoMapper.toDTO(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public CardInfoResponseDTO getCardById(Long id) {

        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_WITH_ID + id));

        return cardInfoMapper.toDTO(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#number")
    public CardInfoResponseDTO getCardByNumber(String number) {

        CardInfo cardInfo = cardInfoRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException(CARD_NOT_FOUND_WITH_NUMBER + number));

        return cardInfoMapper.toDTO(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardInfoResponseDTO> getCardsByIds(List<Long> ids) {

        List<CardInfo> cards = cardInfoRepository.findByIdIn(ids);
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardInfoResponseDTO> getCardsByUserId(Long userId) {

        List<CardInfo> cards = cardInfoRepository.findByUserId(userId);
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all'")
    public List<CardInfoResponseDTO> getAllCards() {

        List<CardInfo> cards = cardInfoRepository.findAll();
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(key = "#id")
    @Caching(evict = {
            @CacheEvict(cacheNames = "cards", allEntries = true),
            @CacheEvict(cacheNames = "users", key = "'all'")
    })
    public CardInfoResponseDTO updateCard(Long id, CardInfoRequestDTO cardInfoRequestDTO) {

        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_WITH_ID + id));

        if (!cardInfo.getNumber().equals(cardInfoRequestDTO.number()) &&
                cardInfoRepository.existsByNumber(cardInfoRequestDTO.number())) {
            throw new DuplicateResourceException(String.format(CARD_ALREADY_EXISTS_WITH_NUMBER, cardInfoRequestDTO.number()));
        }

        cardInfoMapper.updateEntityFromDTO(cardInfoRequestDTO, cardInfo);
        CardInfo updatedCard = cardInfoRepository.save(cardInfo);

        return cardInfoMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "cards", allEntries = true),
            @CacheEvict(cacheNames = "users", key = "'all'")
    })
    public void deleteCard(Long id) {

        if (!cardInfoRepository.existsById(id)) {
            throw new ResourceNotFoundException(CARD_NOT_FOUND_WITH_ID + id);
        }
        cardInfoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean cardExists(Long id) {
        return cardInfoRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean cardNumberExists(String number) {
        return cardInfoRepository.existsByNumber(number);
    }
}