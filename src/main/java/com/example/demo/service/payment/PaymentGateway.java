package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Port for external payment gateway integration.
 */
public interface PaymentGateway {

    GatewayResult charge(PaymentMethodType methodType, BigDecimal amount, Map<String, String> details);

    GatewayResult refund(String transactionRef, BigDecimal amount);

    record GatewayResult(boolean success, String transactionRef, String message) {
    }
}
