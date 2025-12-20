package com.example.PaymentService.repository;

import com.example.PaymentService.entity.Payment;
import com.example.PaymentService.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByUserIdOrOrderIdOrStatus(Long userId, Long orderId, PaymentStatus status);

    @Aggregation(pipeline = {
            "{ $match: { userId: ?0, timestamp: { $gte: ?1, $lte: ?2 }, status: 'SUCCESS' } }",
            "{ $group: { _id: null, total: { $sum: '$paymentAmount' } } }"
    })
    BigDecimal sumPaymentsByUserIdAndDateRange(Long userId, Instant start, Instant end);

    @Aggregation(pipeline = {
            "{ $match:{ timestamp:{ $gte: ?0, $lte: ?1}, status: 'SUCCESS'}}",
            "{ $group:{ _id: null, total: { $sum: '$paymentAmount' } } }"
    })
    BigDecimal sumAllPaymentByDateRange(Instant start,Instant end);
}