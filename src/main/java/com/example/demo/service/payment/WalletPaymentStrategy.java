package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WalletPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentMethodType supports() {
        return PaymentMethodType.WALLET;
    }

    @Override
    public void validate(Map<String, String> details) {
        if (details == null || !details.containsKey("walletId") || details.get("walletId").isBlank()) {
            throw new IllegalArgumentException("walletId is required for WALLET payment");
        }
    }
}
