package com.internship.order_service.mapper;

import com.internship.order_service.dto.OrderRequestDTO;
import com.internship.order_service.dto.OrderResponseDTO;
import com.internship.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.internship.order_service.model.enums.OrderStatus.PENDING)")
    @Mapping(target = "orderItems", source = "orderItems")
    Order toEntity(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO toDTO(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDTO(OrderRequestDTO orderRequestDTO, @MappingTarget Order order);

}
