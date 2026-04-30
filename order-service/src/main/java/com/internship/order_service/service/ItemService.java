package com.internship.order_service.service;

import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.ItemPricePointResponseDto;
import com.internship.order_service.dto.request.UpdateItemPriceRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ItemService {

    Page<ItemDto> getAllAvailableItems(int page, int size);

    ItemDto updateItemPrice(Long itemId, UpdateItemPriceRequestDto updateItemPriceRequestDto);

    List<ItemPricePointResponseDto> getItemPriceHistory(Long itemId, int months);
}

