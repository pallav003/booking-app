package com.example.demo.service.discovery;

import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.TimeSlot;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MaidSearchCriteria(
        String locality,
        ServiceType serviceType,
        TimeSlot requestedSlot,
        Double minRating,
        BigDecimal maxHourlyRate,
        Gender gender
) {
}
