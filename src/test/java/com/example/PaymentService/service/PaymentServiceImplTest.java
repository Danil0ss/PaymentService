package com.example.PaymentService.service;

import com.example.PaymentService.dao.*;
import com.example.PaymentService.entity.Payment;
import com.example.PaymentService.entity.PaymentStatus;
import com.example.PaymentService.mapper.PaymentMapper;
import com.example.PaymentService.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private RestTemplate restTemplate;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentMapper paymentMapper;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequestDTO requestDTO;
    private Payment payment;

    @BeforeEach
    void setUp() {
        requestDTO = new PaymentRequestDTO(1L, 100L, new java.math.BigDecimal("500.0"));
        payment = new Payment();
        payment.setOrderId(100L);
    }

    @Test
    @DisplayName("Успешный платеж (четное число от random.org)")
    void createPayment_Success() {
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("42");
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentMapper.toDTO(any(Payment.class)))
                .thenReturn(new PaymentResponseDTO(
                        "test-id",
                        1L,
                        100L,
                        new BigDecimal("500.0"),
                        PaymentStatus.SUCCESS,
                        Instant.now()
                ));
        PaymentResponseDTO result = paymentService.createPayment(requestDTO);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        verify(kafkaTemplate).send(eq("CREATE_PAYMENT"), any(PaymentEvent.class));
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Ошибка платежа (нечетное число от random.org)")
    void createPayment_Failed_OddNumber() {
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("43");
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.createPayment(requestDTO);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    @DisplayName("Ошибка платежа при сбое внешнего API")
    void createPayment_Failed_ExternalApiError() {
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Down"));
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.createPayment(requestDTO);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }
}