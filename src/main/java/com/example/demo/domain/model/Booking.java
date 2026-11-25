package com.example.demo.domain.model;

import com.example.demo.domain.enums.BookingStatus;
import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private Long id;
    private String customerId;
    private Long maidId;
    private ServiceType serviceType;
    private BookingType bookingType;
    private BookingStatus status;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<BookingOccurrence> occurrences = new ArrayList<>();

    public void refreshStatus() {
        if (occurrences == null || occurrences.isEmpty()) {
            return;
        }
        long cancelledCount = occurrences.stream()
                .filter(o -> o.getStatus() == OccurrenceStatus.CANCELLED)
                .count();
        long confirmedCount = occurrences.stream()
                .filter(o -> o.getStatus() == OccurrenceStatus.CONFIRMED)
                .count();

        if (cancelledCount == occurrences.size()) {
            this.status = BookingStatus.CANCELLED;
        } else if (cancelledCount > 0) {
            this.status = BookingStatus.PARTIALLY_CANCELLED;
        } else if (confirmedCount == occurrences.size()) {
            this.status = BookingStatus.CONFIRMED;
        } else {
            this.status = BookingStatus.PENDING_PAYMENT;
        }
    }
}
