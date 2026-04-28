package com.internship.order_service.mapper;

import com.internship.order_service.dto.request.OrderRequestDto;
import com.internship.order_service.dto.response.OrderResponseDto;
import com.internship.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.internship.order_service.model.enums.OrderStatus.PENDING)")
    @Mapping(target = "orderItems", source = "orderItems")
    Order toEntity(OrderRequestDto orderRequestDto);

    OrderResponseDto toDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDTO(OrderRequestDto orderRequestDto, @MappingTarget Order order);

}


