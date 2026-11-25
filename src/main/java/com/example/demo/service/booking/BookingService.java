package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingStatus;
import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Maid;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MaidRepository;
import com.example.demo.service.concurrency.MaidLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final MaidRepository maidRepository;
    private final BookingRepository bookingRepository;
    private final BookingStrategyFactory strategyFactory;
    private final MaidLockManager lockManager;

    public Booking createBooking(BookingCommand command) {
        return lockManager.executeWithLock(command.maidId(), () -> doCreateBooking(command));
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + id));
    }

    private Booking doCreateBooking(BookingCommand command) {
        Maid maid = maidRepository.findById(command.maidId())
                .orElseThrow(() -> new NoSuchElementException("Maid not found: " + command.maidId()));

        if (!maid.hasSkill(command.serviceType())) {
            throw new IllegalStateException(
                    "Maid " + maid.getId() + " does not offer service: " + command.serviceType());
        }

        BookingStrategy strategy = strategyFactory.getStrategy(command.bookingType());
        List<TimeSlot> requestedSlots = strategy.generateSlots(command);
        List<BookingOccurrence> existingBlocking = bookingRepository.findBlockingOccurrencesByMaidId(maid.getId());

        for (TimeSlot slot : requestedSlots) {
            if (!maid.isWithinAvailability(slot)) {
                throw new IllegalStateException(
                        "Maid is not available during requested window: " + slot.startTime() + " - " + slot.endTime());
            }
            boolean overlaps = existingBlocking.stream()
                    .anyMatch(occ -> occ.getTimeSlot().overlaps(slot));
            if (overlaps) {
                throw new IllegalStateException(
                        "Maid is already booked for overlapping slot: " + slot.startTime() + " - " + slot.endTime());
            }
        }

        List<BookingOccurrence> occurrences = requestedSlots.stream()
                .map(slot -> BookingOccurrence.builder()
                        .maidId(maid.getId())
                        .timeSlot(slot)
                        .amount(calculateAmount(maid.getHourlyRate(), slot))
                        .status(OccurrenceStatus.PENDING_PAYMENT)
                        .build())
                .toList();

        Booking booking = Booking.builder()
                .customerId(command.customerId())
                .maidId(maid.getId())
                .serviceType(command.serviceType())
                .bookingType(command.bookingType())
                .status(BookingStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .occurrences(new java.util.ArrayList<>(occurrences))
                .build();

        return bookingRepository.save(booking);
    }

    private BigDecimal calculateAmount(BigDecimal hourlyRate, TimeSlot slot) {
        return hourlyRate.multiply(slot.durationInHours()).setScale(2, RoundingMode.HALF_UP);
    }
}
