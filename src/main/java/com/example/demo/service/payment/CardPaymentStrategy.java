package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CardPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentMethodType supports() {
        return PaymentMethodType.CARD;
    }

    @Override
    public void validate(Map<String, String> details) {
        if (details == null || !details.containsKey("cardLast4")) {
            throw new IllegalArgumentException("cardLast4 is required for CARD payment");
        }
    }
}
