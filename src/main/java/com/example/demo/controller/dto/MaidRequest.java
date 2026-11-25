package com.example.demo.controller.dto;

import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.AvailabilityWindow;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record MaidRequest(
        @NotBlank String name,
        @NotBlank String locality,
        @NotNull Gender gender,
        double rating,
        @NotNull @Positive BigDecimal hourlyRate,
        @NotEmpty Set<ServiceType> skills,
        @NotEmpty List<AvailabilityWindow> availabilityWindows
) {
}
