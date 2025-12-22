package com.example.PaymentService.repository;

import com.example.PaymentService.dao.TotalSum;
import com.example.PaymentService.entity.Payment;
import com.example.PaymentService.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserIdOrOrderIdOrStatus(Long userId, Long orderId, PaymentStatus status);

    @Aggregation(pipeline = {
            "{ '$match': { 'user_id': ?0, 'timestamp': { '$gte': ?1, '$lte': ?2 }, 'status': 'SUCCESS' } }",
            "{ '$group': { '_id': null, 'total': { '$sum': { '$toDecimal': '$payment_amount' } } } }"
    })
    TotalSum sumPaymentsByUserIdAndDateRange(Long userId, Instant start, Instant end);

    @Aggregation(pipeline = {
            "{ '$match': { 'timestamp': { '$gte': ?0, '$lte': ?1 }, 'status': 'SUCCESS' } }",
            "{ '$group': { '_id': null, 'total': { '$sum': { '$toDecimal': '$payment_amount' } } } }"
    })
    TotalSum sumAllPaymentByDateRange(Instant start, Instant end);

    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'SUCCESS' } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$payment_amount' } } }"
    })
    TotalSum testSumAll();
}