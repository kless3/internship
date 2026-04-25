package com.internship.order_service.service.impl;

import com.internship.order_service.dto.ItemDTO;
import com.internship.order_service.dto.ItemPricePointResponseDto;
import com.internship.order_service.dto.UpdateItemPriceRequestDto;
import com.internship.order_service.exception.ItemValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.mapper.ItemMapper;
import com.internship.order_service.model.Item;
import com.internship.order_service.model.ItemPriceEvent;
import com.internship.order_service.model.enums.ItemPriceEventType;
import com.internship.order_service.repository.ItemPriceEventRepository;
import com.internship.order_service.repository.ItemRepository;
import com.internship.order_service.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String ITEM_NOT_FOUND_WITH_ID = "Item not found with id: ";
    private static final String ITEM_PRICE_SAME = "New item price is the same as current price";
    private static final String MONTHS_RANGE_INVALID = "Months value should be between 1 and 24";

    private final ItemRepository itemRepository;
    private final ItemPriceEventRepository itemPriceEventRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> getAllAvailableItems(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return itemRepository.findAll(pageable)
                .map(itemMapper::toDTO);
    }

    @Override
    @Transactional
    public ItemDTO updateItemPrice(Long itemId, UpdateItemPriceRequestDto updateItemPriceRequestDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND_WITH_ID + itemId));

        if (item.getPrice().compareTo(updateItemPriceRequestDto.price()) == 0) {
            throw new ItemValidationException(ITEM_PRICE_SAME);
        }

        item.setPrice(updateItemPriceRequestDto.price());
        Item savedItem = itemRepository.save(item);
        saveItemPriceEvent(savedItem, ItemPriceEventType.UPDATED);

        return itemMapper.toDTO(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemPricePointResponseDto> getItemPriceHistory(Long itemId, int months) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND_WITH_ID + itemId));
        if (months < 1 || months > 24) {
            throw new ItemValidationException(MONTHS_RANGE_INVALID);
        }

        LocalDateTime fromTimestamp = LocalDateTime.now().minusMonths(months);

        List<ItemPricePointResponseDto> priceHistory = itemPriceEventRepository
                .findAllByItemIdAndEventTimestampGreaterThanEqualOrderByEventTimestampAsc(itemId, fromTimestamp)
                .stream()
                .map(event -> new ItemPricePointResponseDto(event.getEventTimestamp(), event.getPrice()))
                .toList();

        ItemPriceEvent previousSnapshot = itemPriceEventRepository
                .findTopByItemIdAndEventTimestampLessThanOrderByEventTimestampDesc(itemId, fromTimestamp);

        if (previousSnapshot != null) {
            return prependAnchorPoint(priceHistory, fromTimestamp, previousSnapshot.getPrice());
        }

        if (priceHistory.isEmpty()) {
            return List.of(new ItemPricePointResponseDto(fromTimestamp, item.getPrice()));
        }

        return priceHistory;
    }

    private List<ItemPricePointResponseDto> prependAnchorPoint(
            List<ItemPricePointResponseDto> priceHistory,
            LocalDateTime fromTimestamp,
            BigDecimal price
    ) {
        List<ItemPricePointResponseDto> result = new ArrayList<>(priceHistory.size() + 1);
        result.add(new ItemPricePointResponseDto(fromTimestamp, price));
        result.addAll(priceHistory);
        return result;
    }

    private void saveItemPriceEvent(Item item, ItemPriceEventType eventType) {
        ItemPriceEvent event = new ItemPriceEvent();
        event.setItemId(item.getId());
        event.setItemName(item.getName());
        event.setPrice(item.getPrice());
        event.setEventType(eventType);
        itemPriceEventRepository.save(event);
    }
}
