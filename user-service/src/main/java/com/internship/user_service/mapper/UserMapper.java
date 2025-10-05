package com.internship.user_service.mapper;

import com.internship.user_service.dto.UserRequestDTO;
import com.internship.user_service.dto.UserResponseDTO;
import com.internship.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CardInfoMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toEntity(UserRequestDTO userRequestDTO);

    @Mapping(target = "cards", source = "cards")
    UserResponseDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateEntityFromDTO(UserRequestDTO userRequestDTO, @MappingTarget User user);
}