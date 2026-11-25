package com.example.demo.service.cancellation;

import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.model.BookingOccurrence;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Time-based refund policy:
 * - >= 24 hours before start: 100% refund
 * - >= 2 hours before start: 50% refund
 * - < 2 hours before start: 0% refund
 */
@Component
public class TimeBasedCancellationPolicy implements CancellationPolicy {

    @Override
    public BigDecimal calculateRefund(BookingOccurrence occurrence, LocalDateTime cancelledAt) {
        if (occurrence.getStatus() != OccurrenceStatus.CONFIRMED || occurrence.getPaymentId() == null) {
            return BigDecimal.ZERO;
        }

        long hoursUntilStart = Duration.between(cancelledAt, occurrence.getTimeSlot().startTime()).toHours();
        if (hoursUntilStart >= 24) {
            return occurrence.getAmount().setScale(2, RoundingMode.HALF_UP);
        }
        if (hoursUntilStart >= 2) {
            return occurrence.getAmount().multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
