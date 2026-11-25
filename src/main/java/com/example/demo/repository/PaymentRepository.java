package com.example.demo.repository;

import com.example.demo.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
