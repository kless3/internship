package com.internship.user_service.controller;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.exception.DuplicateResourceException;
import com.internship.user_service.exception.ResourceNotFoundException;
import com.internship.user_service.service.CardInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardInfoService cardService;

    @GetMapping
    public ResponseEntity<List<CardInfoResponseDTO>> getAllCards() {
        List<CardInfoResponseDTO> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardInfoResponseDTO> getCardById(@PathVariable Long id) {
        CardInfoResponseDTO card = cardService.getCardById(id);
        if (card == null) {
            throw new ResourceNotFoundException("Card not found with id: " + id);
        }
        return ResponseEntity.ok(card);
    }

    @GetMapping("/number")
    public ResponseEntity<CardInfoResponseDTO> getCardByNumber(@RequestParam String number) {
        CardInfoResponseDTO card = cardService.getCardByNumber(number);
        if (card == null) {
            throw new ResourceNotFoundException("Card not found with number: " + number);
        }
        return ResponseEntity.ok(card);
    }

    @GetMapping("/byIds")
    public ResponseEntity<List<CardInfoResponseDTO>> getCardsByIds(@RequestParam List<Long> ids) {
        List<CardInfoResponseDTO> cards = cardService.getCardsByIds(ids);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardInfoResponseDTO>> getCardsByUserId(@PathVariable Long userId) {
        List<CardInfoResponseDTO> cards = cardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkCardExists(@PathVariable Long id) {
        boolean exists = cardService.cardExists(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/number/{number}/exists")
    public ResponseEntity<Boolean> checkCardNumberExists(@PathVariable String number) {
        boolean exists = cardService.cardNumberExists(number);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CardInfoResponseDTO> createCard(@PathVariable Long userId, @Valid @RequestBody CardInfoRequestDTO cardInfoRequestDTO) {
        try {
            CardInfoResponseDTO createdCard = cardService.createCard(userId, cardInfoRequestDTO);
            return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        } catch (DuplicateResourceException e) {
            throw new DuplicateResourceException("Card with number " + cardInfoRequestDTO.number() + " already exists");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardInfoResponseDTO> updateCard(@PathVariable Long id, @Valid @RequestBody CardInfoRequestDTO cardInfoRequestDTO) {
        try {
            CardInfoResponseDTO updatedCard = cardService.updateCard(id, cardInfoRequestDTO);
            return ResponseEntity.ok(updatedCard);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Card not found with id: " + id);
        } catch (DuplicateResourceException e) {
            throw new DuplicateResourceException("Card number " + cardInfoRequestDTO.number() + " already exists");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        try {
            cardService.deleteCard(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Card not found with id: " + id);
        }
    }
}