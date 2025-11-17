package com.internship.user_service.controller;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.service.CardInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardInfoService cardService;

    @GetMapping
    public ResponseEntity<List<CardInfoResponseDTO>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardInfoResponseDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/number")
    public ResponseEntity<CardInfoResponseDTO> getCardByNumber(@RequestParam String number) {
        return ResponseEntity.ok(cardService.getCardByNumber(number));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<CardInfoResponseDTO>> getCardsByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(cardService.getCardsByIds(ids));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardInfoResponseDTO>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkCardExists(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.cardExists(id));
    }

    @GetMapping("/number/{number}/exists")
    public ResponseEntity<Boolean> checkCardNumberExists(@PathVariable String number) {
        return ResponseEntity.ok(cardService.cardNumberExists(number));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CardInfoResponseDTO> createCard(@PathVariable Long userId, @Valid @RequestBody CardInfoRequestDTO cardInfoRequestDTO) {
        return new ResponseEntity<>(cardService.createCard(userId, cardInfoRequestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardInfoResponseDTO> updateCard(@PathVariable Long id, @Valid @RequestBody CardInfoRequestDTO cardInfoRequestDTO) {
        return ResponseEntity.ok(cardService.updateCard(id, cardInfoRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}