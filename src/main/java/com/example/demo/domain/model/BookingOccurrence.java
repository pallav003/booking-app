package com.example.demo.domain.model;

import com.example.demo.domain.enums.OccurrenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingOccurrence {

    private Long id;
    private Long bookingId;
    private Long maidId;
    private TimeSlot timeSlot;
    private BigDecimal amount;
    private OccurrenceStatus status;
    private Long paymentId;

    public boolean isBlockingSlot() {
        return status == OccurrenceStatus.PENDING_PAYMENT || status == OccurrenceStatus.CONFIRMED;
    }

    public void markConfirmed(Long paymentId) {
        if (status != OccurrenceStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Cannot confirm occurrence in state: " + status);
        }
        this.status = OccurrenceStatus.CONFIRMED;
        this.paymentId = paymentId;
    }

    public void cancel() {
        if (status == OccurrenceStatus.CANCELLED || status == OccurrenceStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot cancel occurrence in state: " + status);
        }
        this.status = OccurrenceStatus.CANCELLED;
    }
}
