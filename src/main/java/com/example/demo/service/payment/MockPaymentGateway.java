package com.example.demo.service.payment;

import com.example.demo.domain.enums.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public GatewayResult charge(PaymentMethodType methodType, BigDecimal amount, Map<String, String> details) {
        String txRef = "TXN-" + methodType.name() + "-" + UUID.randomUUID().toString().substring(0, 8);
        return new GatewayResult(true, txRef, "Payment processed");
    }

    @Override
    public GatewayResult refund(String transactionRef, BigDecimal amount) {
        String refundRef = "REF-" + UUID.randomUUID().toString().substring(0, 8);
        return new GatewayResult(true, refundRef, "Refund processed for " + transactionRef);
    }
}
