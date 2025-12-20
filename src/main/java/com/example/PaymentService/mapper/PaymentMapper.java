package com.example.PaymentService.mapper;

import com.example.PaymentService.dao.PaymentRequestDTO;
import com.example.PaymentService.dao.PaymentResponseDTO;
import com.example.PaymentService.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "paymentAmount", target = "amount")
    PaymentResponseDTO toDTO(Payment payment);

    List<PaymentResponseDTO> toDTO(List<Payment> payments);

    @Mapping(source ="amount",target = "paymentAmount")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Payment toEntity(PaymentRequestDTO dto);


}
