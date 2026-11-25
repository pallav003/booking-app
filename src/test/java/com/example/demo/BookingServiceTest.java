package com.example.demo;

import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.RecurrencePattern;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.AvailabilityWindow;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Maid;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MaidRepository;
import com.example.demo.service.booking.BookingCommand;
import com.example.demo.service.booking.BookingService;
import com.example.demo.service.booking.BookingStrategyFactory;
import com.example.demo.service.booking.InstantBookingStrategy;
import com.example.demo.service.booking.RecurringBookingStrategy;
import com.example.demo.service.booking.ScheduledBookingStrategy;
import com.example.demo.service.concurrency.MaidLockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private MaidRepository maidRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;
    private Maid maid;

    @BeforeEach
    void setUp() {
        BookingStrategyFactory factory = new BookingStrategyFactory(List.of(
                new InstantBookingStrategy(),
                new ScheduledBookingStrategy(),
                new RecurringBookingStrategy()
        ));
        bookingService = new BookingService(maidRepository, bookingRepository, factory, new MaidLockManager());

        maid = Maid.builder()
                .id(1L)
                .name("Asha")
                .locality("Indiranagar")
                .gender(Gender.FEMALE)
                .rating(4.7)
                .hourlyRate(BigDecimal.valueOf(250))
                .skills(Set.of(ServiceType.CLEANING))
                .availabilityWindows(List.of(
                        new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                        new AvailabilityWindow(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
                        new AvailabilityWindow(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))
                ))
                .build();
    }

    @Test
    void createRecurringBooking_generatesExpectedOccurrences() {
        when(maidRepository.findById(1L)).thenReturn(Optional.of(maid));
        when(bookingRepository.findBlockingOccurrencesByMaidId(1L)).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        LocalDateTime mondayMorning = LocalDateTime.of(2026, 11, 30, 9, 0);
        BookingCommand command = BookingCommand.builder()
                .customerId("cust-1")
                .maidId(1L)
                .serviceType(ServiceType.CLEANING)
                .bookingType(BookingType.RECURRING)
                .startTime(mondayMorning)
                .durationMinutes(120)
                .recurrencePattern(RecurrencePattern.WEEKDAYS)
                .occurrences(3)
                .build();

        Booking booking = bookingService.createBooking(command);

        assertEquals(3, booking.getOccurrences().size());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(booking.getOccurrences().get(0).getAmount()));
    }

    @Test
    void createBooking_rejectsDoubleBooking() {
        LocalDateTime mondayMorning = LocalDateTime.of(2026, 11, 30, 9, 0);
        when(maidRepository.findById(1L)).thenReturn(Optional.of(maid));
        when(bookingRepository.findBlockingOccurrencesByMaidId(1L)).thenReturn(List.of(
                BookingOccurrence.builder()
                        .maidId(1L)
                        .timeSlot(new com.example.demo.domain.model.TimeSlot(mondayMorning, mondayMorning.plusHours(2)))
                        .build()
        ));

        BookingCommand command = BookingCommand.builder()
                .customerId("cust-2")
                .maidId(1L)
                .serviceType(ServiceType.CLEANING)
                .bookingType(BookingType.SCHEDULED)
                .startTime(mondayMorning.plusHours(1))
                .durationMinutes(60)
                .build();

        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(command));
    }

    @Test
    void createBooking_concurrentRequests_onlyOneSucceeds() throws InterruptedException {
        List<BookingOccurrence> storedOccurrences = Collections.synchronizedList(new ArrayList<>());
        when(maidRepository.findById(1L)).thenReturn(Optional.of(maid));
        when(bookingRepository.findBlockingOccurrencesByMaidId(1L)).thenAnswer(inv -> new ArrayList<>(storedOccurrences));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            storedOccurrences.addAll(b.getOccurrences());
            return b;
        });

        LocalDateTime mondayMorning = LocalDateTime.of(2026, 11, 30, 10, 0);
        BookingCommand command = BookingCommand.builder()
                .customerId("cust-concurrent")
                .maidId(1L)
                .serviceType(ServiceType.CLEANING)
                .bookingType(BookingType.SCHEDULED)
                .startTime(mondayMorning)
                .durationMinutes(60)
                .build();

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    bookingService.createBooking(command);
                    successCount.incrementAndGet();
                } catch (IllegalStateException ex) {
                    conflictCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(threads - 1, conflictCount.get());
    }
}
