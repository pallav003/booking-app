package com.example.demo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Recurring weekly availability window for a maid (e.g., MONDAY 08:00-18:00).
 */
public record AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

    public AvailabilityWindow {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    public boolean contains(TimeSlot slot) {
        if (!slot.startTime().toLocalDate().equals(slot.endTime().toLocalDate())) {
            return false;
        }
        if (slot.startTime().getDayOfWeek() != dayOfWeek) {
            return false;
        }
        LocalTime slotStart = slot.startTime().toLocalTime();
        LocalTime slotEnd = slot.endTime().toLocalTime();
        return !slotStart.isBefore(startTime) && !slotEnd.isAfter(endTime);
    }
}
