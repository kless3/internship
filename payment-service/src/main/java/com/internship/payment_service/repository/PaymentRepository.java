package com.internship.payment_service.repository;

import com.internship.payment_service.model.Payment;
import com.internship.payment_service.model.enums.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query(value = "{'timestamp': {$gte: ?0, $lte: ?1}, 'status': 'COMPLETED'}", fields = "{'paymentAmount': 1}")
    List<Payment> findCompletedPaymentsInDateRange(LocalDateTime startDate, LocalDateTime endDate);

}
