package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.RecurrencePattern;
import com.example.demo.domain.enums.ServiceType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingCommand(
        String customerId,
        Long maidId,
        ServiceType serviceType,
        BookingType bookingType,
        LocalDateTime startTime,
        Integer durationMinutes,
        RecurrencePattern recurrencePattern,
        Integer occurrences
) {
}
