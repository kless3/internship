package com.internship.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "card_info", indexes = {
        @Index(name = "idx_card_user_id", columnList = "user_id"),
        @Index(name = "idx_card_number", columnList = "number", unique = true),
        @Index(name = "idx_card_expiration_date", columnList = "expiration_date")
})
public class CardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @NotBlank
    @Column(name = "number")
    private String number;

    @NotBlank
    @Column(name = "holder")
    private String holder;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;
}
