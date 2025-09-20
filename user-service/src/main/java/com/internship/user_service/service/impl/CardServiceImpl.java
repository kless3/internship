package com.internship.user_service.service.impl;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.mapper.CardInfoMapper;
import com.internship.user_service.model.CardInfo;
import com.internship.user_service.model.User;
import com.internship.user_service.repository.CardInfoRepository;
import com.internship.user_service.repository.UserRepository;
import com.internship.user_service.service.CardInfoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@CacheConfig(cacheNames = "cards")
@RequiredArgsConstructor
public class CardServiceImpl implements CardInfoService {

    private final CardInfoRepository cardInfoRepository;
    private final UserRepository userRepository;
    private final CardInfoMapper cardInfoMapper;

    @Override
    @Transactional
//    @CacheEvict(allEntries = true)
    public CardInfoResponseDTO createCard(Long userId, CardInfoRequestDTO cardInfoRequestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

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
//    @Cacheable(cacheNames = "#id")
    public CardInfoResponseDTO getCardById(Long id) {

        CardInfo cardInfo = cardInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + id));

        return cardInfoMapper.toDTO(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = "#number")
    public CardInfoResponseDTO getCardByNumber(String number) {

        CardInfo cardInfo = cardInfoRepository.findByNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with number: " + number));

        return cardInfoMapper.toDTO(cardInfo);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(key = "'ids_' + #ids.hashCode()")
    public List<CardInfoResponseDTO> getCardsByIds(List<Long> ids) {

        List<CardInfo> cards = cardInfoRepository.findByIdIn(ids);
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(key = "'user_' + #userId")
    public List<CardInfoResponseDTO> getCardsByUserId(Long userId) {

        List<CardInfo> cards = cardInfoRepository.findByUserId(userId);
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = "'all")
    public List<CardInfoResponseDTO> getAllCards() {

        List<CardInfo> cards = cardInfoRepository.findAll();
        return cards.stream()
                .map(cardInfoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
//    @CachePut(key = "#id")
//    @CacheEvict(key = "'all'")
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
//    @CacheEvict(key = "{'all', #id}")
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