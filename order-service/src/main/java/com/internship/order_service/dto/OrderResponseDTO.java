package com.internship.order_service.dto;

import com.internship.order_service.model.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Long userId;
    private OrderStatus status;
    private LocalDateTime creationDate;
    private List<OrderItemDTO> orderItems;
    private UserInfoDTO userInfoDto;
}
