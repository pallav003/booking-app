package com.example.demo.service.cancellation;

import com.example.demo.domain.model.BookingOccurrence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pluggable policy contract for computing cancellation refunds.
 */
public interface CancellationPolicy {
    BigDecimal calculateRefund(BookingOccurrence occurrence, LocalDateTime cancelledAt);
}
