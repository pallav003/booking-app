package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpiPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentMethodType supports() {
        return PaymentMethodType.UPI;
    }

    @Override
    public void validate(Map<String, String> details) {
        if (details == null || !details.containsKey("upiId") || details.get("upiId").isBlank()) {
            throw new IllegalArgumentException("upiId is required for UPI payment");
        }
    }
}
