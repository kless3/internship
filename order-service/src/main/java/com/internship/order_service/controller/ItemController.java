package com.internship.order_service.controller;

import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.ItemPricePointResponseDto;
import com.internship.order_service.dto.request.UpdateItemPriceRequestDto;
import com.internship.order_service.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Page<ItemDto>> getAvailableItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(itemService.getAllAvailableItems(page, size), HttpStatus.OK);
    }

    @PutMapping("/{itemId}/price")
    public ResponseEntity<ItemDto> updateItemPrice(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemPriceRequestDto updateItemPriceRequestDto
    ) {
        return new ResponseEntity<>(itemService.updateItemPrice(itemId, updateItemPriceRequestDto), HttpStatus.OK);
    }

    @GetMapping("/{itemId}/price/history")
    public ResponseEntity<List<ItemPricePointResponseDto>> getItemPriceHistory(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "6") int months
    ) {
        return new ResponseEntity<>(itemService.getItemPriceHistory(itemId, months), HttpStatus.OK);
    }
}

