package com.internship.order_service.mapper;

import com.internship.order_service.dto.OrderItemDTO;
import com.internship.order_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {
    @Mapping(target = "item", source = "item")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemDTO toDTO(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "item", source = "item")
    OrderItem toEntity(OrderItemDTO orderItemDTO);
}