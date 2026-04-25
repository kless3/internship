package com.internship.order_service.service;

import com.internship.order_service.dto.ItemDTO;
import com.internship.order_service.dto.ItemPricePointResponseDto;
import com.internship.order_service.dto.UpdateItemPriceRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ItemService {

    Page<ItemDTO> getAllAvailableItems(int page, int size);

    ItemDTO updateItemPrice(Long itemId, UpdateItemPriceRequestDto updateItemPriceRequestDto);

    List<ItemPricePointResponseDto> getItemPriceHistory(Long itemId, int months);
}
