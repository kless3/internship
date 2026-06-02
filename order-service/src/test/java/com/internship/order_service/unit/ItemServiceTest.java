package com.internship.order_service.unit;

import com.internship.order_service.dto.request.UpdateItemPriceRequestDto;
import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.dto.response.ItemPricePointResponseDto;
import com.internship.order_service.exception.ItemValidationException;
import com.internship.order_service.exception.ResourceNotFoundException;
import com.internship.order_service.mapper.ItemMapper;
import com.internship.order_service.model.Item;
import com.internship.order_service.model.ItemPriceEvent;
import com.internship.order_service.model.enums.ItemPriceEventType;
import com.internship.order_service.repository.ItemPriceEventRepository;
import com.internship.order_service.repository.ItemRepository;
import com.internship.order_service.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemPriceEventRepository itemPriceEventRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item(1L, "Laptop", new BigDecimal("100.00"));
    }

    @Test
    void updateItemPrice_shouldUpdateAndSaveEvent() {
        UpdateItemPriceRequestDto request = new UpdateItemPriceRequestDto(new BigDecimal("120.00"));
        ItemDto expectedDto = new ItemDto(1L, "Laptop", new BigDecimal("120.00"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemMapper.toDTO(any(Item.class))).thenReturn(expectedDto);

        ItemDto result = itemService.updateItemPrice(1L, request);

        assertEquals(new BigDecimal("120.00"), result.price());
        verify(itemPriceEventRepository).save(any(ItemPriceEvent.class));
    }

    @Test
    void updateItemPrice_shouldThrowWhenPriceSame() {
        UpdateItemPriceRequestDto request = new UpdateItemPriceRequestDto(new BigDecimal("100.00"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemValidationException ex = assertThrows(ItemValidationException.class,
                () -> itemService.updateItemPrice(1L, request));

        assertEquals("New item price is the same as current price", ex.getMessage());
    }

    @Test
    void updateItemPrice_shouldThrowWhenItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> itemService.updateItemPrice(999L, new UpdateItemPriceRequestDto(new BigDecimal("120.00"))));

        assertEquals("Item not found with id: 999", ex.getMessage());
    }

    @Test
    void getItemPriceHistory_shouldThrowWhenMonthsOutOfRange() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemValidationException ex = assertThrows(ItemValidationException.class,
                () -> itemService.getItemPriceHistory(1L, 25));

        assertEquals("Months value should be between 1 and 24", ex.getMessage());
    }

    @Test
    void getItemPriceHistory_shouldReturnAnchorWhenNoEvents() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemPriceEventRepository.findAllByItemIdAndEventTimestampGreaterThanEqualOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of());
        when(itemPriceEventRepository.findTopByItemIdAndEventTimestampLessThanOrderByEventTimestampDesc(any(), any()))
                .thenReturn(null);

        List<ItemPricePointResponseDto> history = itemService.getItemPriceHistory(1L, 6);

        assertEquals(1, history.size());
        assertEquals(new BigDecimal("100.00"), history.get(0).price());
    }

    @Test
    void getItemPriceHistory_shouldPrependAnchorWhenPreviousSnapshotExists() {
        ItemPriceEvent inRange = new ItemPriceEvent();
        inRange.setEventTimestamp(LocalDateTime.now().minusDays(5));
        inRange.setPrice(new BigDecimal("120.00"));

        ItemPriceEvent previous = new ItemPriceEvent();
        previous.setPrice(new BigDecimal("90.00"));
        previous.setEventType(ItemPriceEventType.INITIALIZED);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemPriceEventRepository.findAllByItemIdAndEventTimestampGreaterThanEqualOrderByEventTimestampAsc(any(), any()))
                .thenReturn(List.of(inRange));
        when(itemPriceEventRepository.findTopByItemIdAndEventTimestampLessThanOrderByEventTimestampDesc(any(), any()))
                .thenReturn(previous);

        List<ItemPricePointResponseDto> history = itemService.getItemPriceHistory(1L, 6);

        assertFalse(history.isEmpty());
        assertEquals(new BigDecimal("90.00"), history.get(0).price());
        assertEquals(new BigDecimal("120.00"), history.get(1).price());
    }
}
