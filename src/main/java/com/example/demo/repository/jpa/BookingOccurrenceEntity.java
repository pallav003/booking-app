package com.example.demo.repository.jpa;

import com.example.demo.domain.enums.OccurrenceStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_occurrences")
@Getter
@Setter
@NoArgsConstructor
public class BookingOccurrenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    private Long maidId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OccurrenceStatus status;

    private Long paymentId;
}
