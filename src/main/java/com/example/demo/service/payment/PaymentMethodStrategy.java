package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;

import java.util.Map;

/**
 * Strategy contract for payment method-specific validation/handling.
 */
public interface PaymentMethodStrategy {
    PaymentMethodType supports();

    void validate(Map<String, String> details);
}
