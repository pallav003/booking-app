package com.example.demo.repository.jpa;

import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingOccurrenceJpaRepository occurrenceJpaRepository;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = toEntity(booking);
        return toDomain(bookingJpaRepository.save(entity));
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BookingOccurrence> findBlockingOccurrencesByMaidId(Long maidId) {
        return occurrenceJpaRepository
                .findByMaidIdAndStatusIn(maidId, List.of(OccurrenceStatus.PENDING_PAYMENT, OccurrenceStatus.CONFIRMED))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private BookingEntity toEntity(Booking booking) {
        BookingEntity entity = new BookingEntity();
        entity.setId(booking.getId());
        entity.setCustomerId(booking.getCustomerId());
        entity.setMaidId(booking.getMaidId());
        entity.setServiceType(booking.getServiceType());
        entity.setBookingType(booking.getBookingType());
        entity.setStatus(booking.getStatus());
        entity.setCreatedAt(booking.getCreatedAt());

        List<BookingOccurrenceEntity> occurrenceEntities = booking.getOccurrences().stream()
                .map(occ -> {
                    BookingOccurrenceEntity oe = new BookingOccurrenceEntity();
                    oe.setId(occ.getId());
                    oe.setBooking(entity);
                    oe.setMaidId(occ.getMaidId());
                    oe.setStartTime(occ.getTimeSlot().startTime());
                    oe.setEndTime(occ.getTimeSlot().endTime());
                    oe.setAmount(occ.getAmount());
                    oe.setStatus(occ.getStatus());
                    oe.setPaymentId(occ.getPaymentId());
                    return oe;
                })
                .toList();
        entity.setOccurrences(new java.util.ArrayList<>(occurrenceEntities));
        return entity;
    }

    private Booking toDomain(BookingEntity entity) {
        return Booking.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .maidId(entity.getMaidId())
                .serviceType(entity.getServiceType())
                .bookingType(entity.getBookingType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .occurrences(new java.util.ArrayList<>(entity.getOccurrences().stream()
                        .map(this::toDomain)
                        .toList()))
                .build();
    }

    private BookingOccurrence toDomain(BookingOccurrenceEntity entity) {
        return BookingOccurrence.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking().getId())
                .maidId(entity.getMaidId())
                .timeSlot(new TimeSlot(entity.getStartTime(), entity.getEndTime()))
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paymentId(entity.getPaymentId())
                .build();
    }
}
