package com.internship.order_service.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
