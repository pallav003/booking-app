package com.example.demo.service.payment;

import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.enums.PaymentMethodType;
import com.example.demo.domain.enums.PaymentStatus;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Payment;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final Map<PaymentMethodType, PaymentMethodStrategy> methodStrategies;

    public PaymentService(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            List<PaymentMethodStrategy> strategies) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.methodStrategies = strategies.stream()
                .collect(Collectors.toMap(PaymentMethodStrategy::supports, Function.identity()));
    }

    public Payment processPayment(
            Long bookingId,
            Long occurrenceId,
            PaymentMethodType methodType,
            String idempotencyKey,
            Map<String, String> paymentDetails) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        PaymentMethodStrategy strategy = methodStrategies.get(methodType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + methodType);
        }
        strategy.validate(paymentDetails);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));

        List<BookingOccurrence> targetOccurrences = resolveTargetOccurrences(booking, occurrenceId);
        if (targetOccurrences.isEmpty()) {
            throw new IllegalStateException("No pending occurrences available to pay");
        }

        BigDecimal totalAmount = targetOccurrences.stream()
                .map(BookingOccurrence::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentGateway.GatewayResult result = paymentGateway.charge(methodType, totalAmount, paymentDetails);
        PaymentStatus status = result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        Payment savedPayment = paymentRepository.save(Payment.builder()
                .bookingId(bookingId)
                .occurrenceId(occurrenceId)
                .amount(totalAmount)
                .refundedAmount(BigDecimal.ZERO)
                .methodType(methodType)
                .status(status)
                .idempotencyKey(idempotencyKey)
                .transactionRef(result.transactionRef())
                .createdAt(LocalDateTime.now())
                .build());

        if (!result.success()) {
            throw new IllegalStateException("Payment failed: " + result.message());
        }

        targetOccurrences.forEach(occ -> occ.markConfirmed(savedPayment.getId()));
        booking.refreshStatus();
        bookingRepository.save(booking);

        return savedPayment;
    }

    private List<BookingOccurrence> resolveTargetOccurrences(Booking booking, Long occurrenceId) {
        if (occurrenceId != null) {
            BookingOccurrence occurrence = booking.getOccurrences().stream()
                    .filter(occ -> occ.getId().equals(occurrenceId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Occurrence not found: " + occurrenceId));
            if (occurrence.getStatus() != OccurrenceStatus.PENDING_PAYMENT) {
                throw new IllegalStateException(
                        "Occurrence is not pending payment: " + occurrence.getStatus());
            }
            return List.of(occurrence);
        }

        return booking.getOccurrences().stream()
                .filter(occ -> occ.getStatus() == OccurrenceStatus.PENDING_PAYMENT)
                .toList();
    }
}
