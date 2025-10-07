package com.internship.user_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserResponseDTO {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<CardInfoResponseDTO> cards;
}