package com.example.PaymentService.controller;

import com.example.PaymentService.dao.PaymentRequestDTO;
import com.example.PaymentService.dao.PaymentResponseDTO;
import com.example.PaymentService.entity.PaymentStatus;
import com.example.PaymentService.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO requestDTO){
        PaymentResponseDTO createdPayment=paymentService.createPayment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> findPayment(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) PaymentStatus status){
        List<PaymentResponseDTO> payments =paymentService.getPayments(userId, orderId, status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/total/user/{userId}")
    public ResponseEntity<BigDecimal> getSumById(
            @PathVariable Long userId,
            @RequestParam Instant from,
            @RequestParam Instant to){
        BigDecimal sum=paymentService.getTotalSumByUserId(userId,from,to);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/total/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getAllSum(
            @RequestParam Instant from,
            @RequestParam Instant to){
        paymentService.debugData();
        BigDecimal sum=paymentService.getTotalSumAll(from,to);
        return ResponseEntity.ok(sum);
    }
}
