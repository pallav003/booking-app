package com.example.demo.domain.model;

import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Maid {

    private Long id;
    private String name;
    private String locality;
    private Gender gender;
    private double rating;
    private BigDecimal hourlyRate;

    @Builder.Default
    private Set<ServiceType> skills = new HashSet<>();

    @Builder.Default
    private List<AvailabilityWindow> availabilityWindows = new ArrayList<>();

    public boolean hasSkill(ServiceType serviceType) {
        return skills != null && skills.contains(serviceType);
    }

    public boolean isWithinAvailability(TimeSlot slot) {
        return availabilityWindows != null
                && availabilityWindows.stream().anyMatch(window -> window.contains(slot));
    }
}
