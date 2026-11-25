package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.model.TimeSlot;

import java.util.List;

/**
 * Strategy contract for generating time slots for different booking types.
 */
public interface BookingStrategy {
    BookingType supports();

    List<TimeSlot> generateSlots(BookingCommand command);
}
