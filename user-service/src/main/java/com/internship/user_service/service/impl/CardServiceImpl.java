package com.internship.user_service.service.impl;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.mapper.CardInfoMapper;
import com.internship.user_service.model.CardInfo;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardInfoRepository;
import com.internship.user_service.service.CardInfoService;
import com.internship.user_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@CacheConfig(cacheNames = "cards")
@RequiredArgsConstructor
public class CardServiceImpl implements CardInfoService {

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

        if (cardInfoRepository.existsByNumber(cardInfoRequestDTO.getNumber())) {
            throw new IllegalArgumentException("Card with number " + cardInfoRequestDTO.getNumber() + " already exists");
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
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));

        return cardInfoMapper.toDTO(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#number")
    public CardInfoResponseDTO getCardByNumber(String number) {

        CardInfo cardInfo = cardInfoRepository.findByNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with number: " + number));

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
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));

        if (!cardInfo.getNumber().equals(cardInfoRequestDTO.getNumber()) &&
                cardInfoRepository.existsByNumber(cardInfoRequestDTO.getNumber())) {
            throw new IllegalArgumentException("Card number " + cardInfoRequestDTO.getNumber() + " already exists");
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
            throw new EntityNotFoundException("Card not found with id: " + id);
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