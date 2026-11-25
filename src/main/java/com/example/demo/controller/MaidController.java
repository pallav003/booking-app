package com.example.demo.controller;

import com.example.demo.controller.dto.ApiResponse;
import com.example.demo.controller.dto.MaidRequest;
import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.Maid;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.service.discovery.MaidSearchCriteria;
import com.example.demo.service.discovery.MaidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/maids")
@RequiredArgsConstructor
public class MaidController {

    private final MaidService maidService;

    @PostMapping
    public ResponseEntity<ApiResponse<Maid>> onboardMaid(@Valid @RequestBody MaidRequest request) {
        Maid maid = Maid.builder()
                .name(request.name())
                .locality(request.locality())
                .gender(request.gender())
                .rating(request.rating())
                .hourlyRate(request.hourlyRate())
                .skills(request.skills())
                .availabilityWindows(request.availabilityWindows())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Maid onboarded", maidService.onboardMaid(maid)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Maid>> getMaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(maidService.getMaid(id)));
    }

    @GetMapping("/discover")
    public ResponseEntity<ApiResponse<List<Maid>>> discoverMaids(
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxHourlyRate,
            @RequestParam(required = false) Gender gender) {

        TimeSlot requestedSlot = null;
        if (startTime != null && durationMinutes != null) {
            requestedSlot = new TimeSlot(startTime, startTime.plusMinutes(durationMinutes));
        }

        MaidSearchCriteria criteria = MaidSearchCriteria.builder()
                .locality(locality)
                .serviceType(serviceType)
                .requestedSlot(requestedSlot)
                .minRating(minRating)
                .maxHourlyRate(maxHourlyRate)
                .gender(gender)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(maidService.discoverMaids(criteria)));
    }
}
