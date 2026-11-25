package com.example.demo.repository.jpa;

import com.example.demo.domain.enums.OccurrenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookingOccurrenceJpaRepository extends JpaRepository<BookingOccurrenceEntity, Long> {
    List<BookingOccurrenceEntity> findByMaidIdAndStatusIn(Long maidId, Collection<OccurrenceStatus> statuses);
}
