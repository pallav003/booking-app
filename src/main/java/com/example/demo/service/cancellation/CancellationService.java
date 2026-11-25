package com.example.demo.service.cancellation;

import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.enums.PaymentStatus;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Payment;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final CancellationPolicy cancellationPolicy;

    public CancellationResult cancelBooking(Long bookingId, Long occurrenceId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));

        List<BookingOccurrence> toCancel = resolveOccurrencesToCancel(booking, occurrenceId);
        if (toCancel.isEmpty()) {
            throw new IllegalStateException("No cancellable occurrences found for booking: " + bookingId);
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalRefund = BigDecimal.ZERO;
        List<Long> cancelledIds = new ArrayList<>();

        for (BookingOccurrence occ : toCancel) {
            BigDecimal refundAmount = cancellationPolicy.calculateRefund(occ, now);
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && occ.getPaymentId() != null) {
                processRefund(occ.getPaymentId(), refundAmount);
                totalRefund = totalRefund.add(refundAmount);
            }
            occ.cancel();
            cancelledIds.add(occ.getId());
        }

        booking.refreshStatus();
        bookingRepository.save(booking);

        return new CancellationResult(booking.getId(), cancelledIds, totalRefund, booking.getStatus());
    }

    private List<BookingOccurrence> resolveOccurrencesToCancel(Booking booking, Long occurrenceId) {
        if (occurrenceId != null) {
            BookingOccurrence occ = booking.getOccurrences().stream()
                    .filter(o -> o.getId().equals(occurrenceId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Occurrence not found: " + occurrenceId));
            return List.of(occ);
        }

        return booking.getOccurrences().stream()
                .filter(occ -> occ.getStatus() != OccurrenceStatus.CANCELLED
                        && occ.getStatus() != OccurrenceStatus.COMPLETED)
                .toList();
    }

    private void processRefund(Long paymentId, BigDecimal refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));
        paymentGateway.refund(payment.getTransactionRef(), refundAmount);
        BigDecimal currentRefunded = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO;
        payment.setRefundedAmount(currentRefunded.add(refundAmount));
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }
}
