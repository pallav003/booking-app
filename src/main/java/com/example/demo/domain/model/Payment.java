package com.example.demo.domain.model;

import com.example.demo.domain.enums.PaymentMethodType;
import com.example.demo.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private Long id;
    private Long bookingId;
    private Long occurrenceId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private PaymentMethodType methodType;
    private PaymentStatus status;
    private String idempotencyKey;
    private String transactionRef;
    private LocalDateTime createdAt;
}
