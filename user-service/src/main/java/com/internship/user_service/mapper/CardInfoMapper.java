package com.internship.user_service.mapper;

import com.internship.user_service.dto.CardInfoRequestDTO;
import com.internship.user_service.dto.CardInfoResponseDTO;
import com.internship.user_service.model.CardInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardInfoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    CardInfo toEntity(CardInfoRequestDTO cardInfoRequestDTO);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "holder", source = "holder")
    CardInfoResponseDTO toDTO(CardInfo cardInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDTO(CardInfoRequestDTO cardInfoRequestDTO, @MappingTarget CardInfo cardInfo);
}