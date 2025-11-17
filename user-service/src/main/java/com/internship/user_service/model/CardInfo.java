package com.internship.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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