package com.example.PaymentService.dao;

import com.example.PaymentService.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponseDTO(
        String id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentStatus status,
        OffsetDateTime timestamp
) {}