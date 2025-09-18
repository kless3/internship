package com.internship.user_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CardInfoResponseDTO {
    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Long userId;
}
