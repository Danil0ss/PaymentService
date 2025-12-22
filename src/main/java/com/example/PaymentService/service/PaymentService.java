package com.example.PaymentService.service;

import com.example.PaymentService.dao.PaymentRequestDTO;
import com.example.PaymentService.dao.PaymentResponseDTO;
import com.example.PaymentService.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentService {


    PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO);


    List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus status);


    BigDecimal getTotalSumByUserId(Long userId, Instant from, Instant to);


    BigDecimal getTotalSumAll(Instant from, Instant to);

    public void debugData();
}