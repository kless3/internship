package com.internship.payment_service.mapper;

import com.internship.payment_service.dto.PaymentRequestDTO;
import com.internship.payment_service.dto.PaymentResponseDTO;
import com.internship.payment_service.model.Payment;
import com.internship.payment_service.model.enums.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {PaymentStatus.class, LocalDateTime.class})
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(PaymentStatus.PENDING)")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    Payment toEntity(PaymentRequestDTO dto);

    PaymentResponseDTO toResponseDTO(Payment payment);

}