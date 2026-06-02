package com.internship.order_service.mapper;

import com.internship.order_service.dto.response.ItemDto;
import com.internship.order_service.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    ItemDto toDTO(Item item);
}

