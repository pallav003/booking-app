package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class InstantBookingStrategy implements BookingStrategy {

    @Override
    public BookingType supports() {
        return BookingType.INSTANT;
    }

    @Override
    public List<TimeSlot> generateSlots(BookingCommand command) {
        LocalDateTime start = command.startTime() != null ? command.startTime() : LocalDateTime.now();
        int duration = validateDuration(command.durationMinutes());
        return List.of(new TimeSlot(start, start.plusMinutes(duration)));
    }

    private int validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
        return durationMinutes;
    }
}
