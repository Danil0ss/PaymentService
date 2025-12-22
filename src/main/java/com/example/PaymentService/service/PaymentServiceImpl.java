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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final RestTemplate restTemplate;
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String URL = "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";


    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {
        Payment payment = paymentMapper.toEntity(requestDTO);
        payment.setTimestamp(Instant.now());
        try {
            String response = restTemplate.getForObject(URL, String.class);
            int randomNumber = Integer.parseInt(response.trim());
            if (randomNumber % 2 == 0) {
                payment.setStatus(PaymentStatus.SUCCESS);
            } else payment.setStatus(PaymentStatus.FAILED);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
        }
        Payment savedPayment = paymentRepository.save(payment);
        PaymentEvent event = new PaymentEvent(savedPayment.getOrderId(), savedPayment.getStatus().name());
        kafkaTemplate.send("CREATE_PAYMENT", event);
        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    public List<PaymentResponseDTO> getPayments(Long userId, Long orderId, PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByUserIdOrOrderIdOrStatus(userId, orderId, status);
        return paymentMapper.toDTO(payments);
    }

    @Override
    public BigDecimal getTotalSumByUserId(Long userId, Instant from, Instant to) {
        TotalSum result = paymentRepository.sumPaymentsByUserIdAndDateRange(userId, from, to);
        return (result != null && result.getTotal() != null) ? result.getTotal() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalSumAll(Instant from, Instant to) {
        TotalSum result = paymentRepository.sumAllPaymentByDateRange(from, to);
        return (result != null && result.getTotal() != null) ? result.getTotal() : BigDecimal.ZERO;
    }

    public void debugData() {
        List<org.bson.Document> allDocs = mongoTemplate.findAll(org.bson.Document.class, "payments");
        allDocs.forEach(doc -> System.out.println("DEBUG DOC: " + doc.toJson()));
    }
}
