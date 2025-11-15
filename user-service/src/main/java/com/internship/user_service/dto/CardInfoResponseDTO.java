package com.internship.user_service.dto;

import java.time.LocalDate;

public record CardInfoResponseDTO(
        Long id,
        String number,
        String holder,
        LocalDate expirationDate,
        Long userId
) {}