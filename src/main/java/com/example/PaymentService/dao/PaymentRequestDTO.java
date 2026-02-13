package com.example.PaymentService.dao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequestDTO(

        @NotNull
        Long orderId,
        @NotNull
        Long userId,
        @Positive()
        BigDecimal amount
) {
}
