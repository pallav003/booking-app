package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledBookingStrategy implements BookingStrategy {

    @Override
    public BookingType supports() {
        return BookingType.SCHEDULED;
    }

    @Override
    public List<TimeSlot> generateSlots(BookingCommand command) {
        if (command.startTime() == null) {
            throw new IllegalArgumentException("startTime is required for scheduled booking");
        }
        if (command.durationMinutes() == null || command.durationMinutes() <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
        return List.of(new TimeSlot(command.startTime(), command.startTime().plusMinutes(command.durationMinutes())));
    }
}
