package com.example.demo.controller.dto;

import com.example.demo.domain.enums.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PaymentRequest(
        @NotNull Long bookingId,
        Long occurrenceId,
        @NotNull PaymentMethodType paymentMethod,
        String idempotencyKey,
        Map<String, String> details
) {
}
