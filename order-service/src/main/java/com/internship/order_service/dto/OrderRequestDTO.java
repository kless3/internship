package com.internship.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "User email cannot be blank")
    @Email(message = "User email should be valid")
    private String userEmail;

    @Valid
    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemDTO> orderItems;

}
