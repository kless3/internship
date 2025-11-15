package com.internship.user_service.dto;

import java.time.LocalDate;
import java.util.List;

public record UserResponseDTO(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        List<CardInfoResponseDTO> cards
) {}