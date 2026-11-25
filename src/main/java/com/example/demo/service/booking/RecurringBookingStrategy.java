package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.RecurrencePattern;
import com.example.demo.domain.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringBookingStrategy implements BookingStrategy {

    @Override
    public BookingType supports() {
        return BookingType.RECURRING;
    }

    @Override
    public List<TimeSlot> generateSlots(BookingCommand command) {
        if (command.startTime() == null) {
            throw new IllegalArgumentException("startTime is required for recurring booking");
        }
        if (command.durationMinutes() == null || command.durationMinutes() <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
        if (command.recurrencePattern() == null) {
            throw new IllegalArgumentException("recurrencePattern is required for recurring booking");
        }
        int count = command.occurrences() != null ? command.occurrences() : 4;
        if (count <= 0) {
            throw new IllegalArgumentException("occurrences must be positive");
        }

        List<TimeSlot> slots = new ArrayList<>(count);
        LocalDateTime current = command.startTime();

        while (slots.size() < count) {
            if (matchesPattern(current, command.recurrencePattern())) {
                slots.add(new TimeSlot(current, current.plusMinutes(command.durationMinutes())));
            }
            current = advance(current, command.recurrencePattern());
        }
        return slots;
    }

    private boolean matchesPattern(LocalDateTime dateTime, RecurrencePattern pattern) {
        if (pattern == RecurrencePattern.WEEKDAYS) {
            DayOfWeek day = dateTime.getDayOfWeek();
            return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        }
        return true;
    }

    private LocalDateTime advance(LocalDateTime current, RecurrencePattern pattern) {
        return switch (pattern) {
            case DAILY, WEEKDAYS -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
        };
    }
}
