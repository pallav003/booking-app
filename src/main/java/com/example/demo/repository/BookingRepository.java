package com.example.demo.repository;

import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<BookingOccurrence> findBlockingOccurrencesByMaidId(Long maidId);
}
