package com.example.PaymentService.service;

import com.example.PaymentService.dao.PaymentEvent;
import com.example.PaymentService.dao.PaymentRequestDTO;
import com.example.PaymentService.dao.PaymentResponseDTO;
import com.example.PaymentService.dao.TotalSum;
import com.example.PaymentService.entity.Payment;
import com.example.PaymentService.entity.PaymentStatus;
import com.example.PaymentService.mapper.PaymentMapper;
import com.example.PaymentService.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String BANK_SIMULATION_URL = "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {
        Payment payment = paymentMapper.toEntity(requestDTO);
        payment.setTimestamp(Instant.now());
        payment.setStatus(PaymentStatus.NEW);

        try {
            String response = restTemplate.getForObject(BANK_SIMULATION_URL, String.class);
            if (response != null) {
                int randomNumber = Integer.parseInt(response.trim());
                if (randomNumber % 2 == 0) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                }
            }
        } catch (Exception e) {
            log.error("Bank simulation failed, payment rejected: {}", e.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent(
                savedPayment.getOrderId(),
                savedPayment.getStatus().name()
        );

        kafkaTemplate.send("payment-events", event);

        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    public List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus status) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        if (userId != null) {
            criteria.add(Criteria.where("userId").is(userId));
        }
        if (orderId != null) {
            criteria.add(Criteria.where("orderId").is(orderId));
        }
        if (status != null) {
            criteria.add(Criteria.where("status").is(status));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        List<Payment> payments = mongoTemplate.find(query, Payment.class);
        return paymentMapper.toDTO(payments);
    }

    @Override
    public BigDecimal getTotalSumByUserId(Long userId, Instant from, Instant to) {
        TotalSum result = paymentRepository.sumPaymentsByUserIdAndDateRange(userId, from, to);
        return extractTotal(result);
    }

    @Override
    public BigDecimal getTotalSumAll(Instant from, Instant to) {
        TotalSum result = paymentRepository.sumAllPaymentByDateRange(from, to);
        return extractTotal(result);
    }

    private BigDecimal extractTotal(TotalSum result) {
        return (result != null && result.getTotal() != null) ? result.getTotal() : BigDecimal.ZERO;
    }

    @Override
    public void debugData() {
        mongoTemplate.getCollection("payments").find()
                .forEach(doc -> System.out.println("DEBUG MONGO DOC: " + doc.toJson()));
    }
}