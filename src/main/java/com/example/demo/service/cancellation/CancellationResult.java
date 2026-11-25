package com.example.demo.service.cancellation;

import com.example.demo.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

public record CancellationResult(
        Long bookingId,
        List<Long> cancelledOccurrenceIds,
        BigDecimal totalRefundAmount,
        BookingStatus bookingStatus
) {
}
