package com.example.demo.controller.dto;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.RecurrencePattern;
import com.example.demo.domain.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotBlank String customerId,
        @NotNull Long maidId,
        @NotNull ServiceType serviceType,
        @NotNull BookingType bookingType,
        LocalDateTime startTime,
        @NotNull @Positive Integer durationMinutes,
        RecurrencePattern recurrencePattern,
        Integer occurrences
) {
}
