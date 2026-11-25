package com.example.demo;

import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.AvailabilityWindow;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Maid;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MaidRepository;
import com.example.demo.service.discovery.GenderFilter;
import com.example.demo.service.discovery.LocalityFilter;
import com.example.demo.service.discovery.MaidSearchCriteria;
import com.example.demo.service.discovery.MaidService;
import com.example.demo.service.discovery.PriceFilter;
import com.example.demo.service.discovery.RatingFilter;
import com.example.demo.service.discovery.ServiceTypeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaidServiceTest {

    @Mock
    private MaidRepository maidRepository;

    @Mock
    private BookingRepository bookingRepository;

    private MaidService maidService;

    @BeforeEach
    void setUp() {
        maidService = new MaidService(
                maidRepository,
                bookingRepository,
                List.of(
                        new LocalityFilter(),
                        new ServiceTypeFilter(),
                        new RatingFilter(),
                        new PriceFilter(),
                        new GenderFilter()
                )
        );
    }

    @Test
    void discoverMaids_filtersByCriteriaAndExcludesBookedMaids() {
        Maid availableMaid = Maid.builder()
                .id(1L)
                .name("Asha")
                .locality("Koramangala")
                .gender(Gender.FEMALE)
                .rating(4.8)
                .hourlyRate(BigDecimal.valueOf(300))
                .skills(Set.of(ServiceType.CLEANING, ServiceType.COOKING))
                .availabilityWindows(List.of(
                        new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))
                ))
                .build();

        Maid bookedMaid = Maid.builder()
                .id(2L)
                .name("Meena")
                .locality("Koramangala")
                .gender(Gender.FEMALE)
                .rating(4.9)
                .hourlyRate(BigDecimal.valueOf(350))
                .skills(Set.of(ServiceType.CLEANING))
                .availabilityWindows(List.of(
                        new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))
                ))
                .build();

        when(maidRepository.findAll()).thenReturn(List.of(availableMaid, bookedMaid));

        LocalDateTime mondayNineAm = LocalDateTime.of(2026, 11, 30, 9, 0);
        TimeSlot requestedSlot = new TimeSlot(mondayNineAm, mondayNineAm.plusHours(2));

        when(bookingRepository.findBlockingOccurrencesByMaidId(1L)).thenReturn(List.of());
        when(bookingRepository.findBlockingOccurrencesByMaidId(2L)).thenReturn(List.of(
                BookingOccurrence.builder()
                        .id(10L)
                        .maidId(2L)
                        .timeSlot(new TimeSlot(mondayNineAm, mondayNineAm.plusHours(1)))
                        .status(OccurrenceStatus.CONFIRMED)
                        .build()
        ));

        MaidSearchCriteria criteria = MaidSearchCriteria.builder()
                .locality("koramangala")
                .serviceType(ServiceType.CLEANING)
                .minRating(4.5)
                .maxHourlyRate(BigDecimal.valueOf(400))
                .gender(Gender.FEMALE)
                .requestedSlot(requestedSlot)
                .build();

        List<Maid> result = maidService.discoverMaids(criteria);

        assertEquals(1, result.size());
        assertEquals("Asha", result.get(0).getName());
    }
}
