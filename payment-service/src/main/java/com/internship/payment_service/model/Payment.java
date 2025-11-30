package com.internship.payment_service.model;

import com.internship.payment_service.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    private String id;

    @Indexed
    @Field("order_id")
    private Long orderId;

    @Indexed
    @Field("user_id")
    private Long userId;

    @Indexed
    @Field("status")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Field("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Field("payment_amount")
    private BigDecimal paymentAmount;

}
