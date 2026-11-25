package com.example.demo.repository.jpa;

import com.example.demo.domain.model.Payment;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = toEntity(payment);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    private PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId());
        entity.setBookingId(payment.getBookingId());
        entity.setOccurrenceId(payment.getOccurrenceId());
        entity.setAmount(payment.getAmount());
        entity.setRefundedAmount(payment.getRefundedAmount());
        entity.setMethodType(payment.getMethodType());
        entity.setStatus(payment.getStatus());
        entity.setIdempotencyKey(payment.getIdempotencyKey());
        entity.setTransactionRef(payment.getTransactionRef());
        entity.setCreatedAt(payment.getCreatedAt());
        return entity;
    }

    private Payment toDomain(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .bookingId(entity.getBookingId())
                .occurrenceId(entity.getOccurrenceId())
                .amount(entity.getAmount())
                .refundedAmount(entity.getRefundedAmount())
                .methodType(entity.getMethodType())
                .status(entity.getStatus())
                .idempotencyKey(entity.getIdempotencyKey())
                .transactionRef(entity.getTransactionRef())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
