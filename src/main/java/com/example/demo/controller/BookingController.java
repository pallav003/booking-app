package com.example.demo.controller;

import com.example.demo.controller.dto.ApiResponse;
import com.example.demo.controller.dto.BookingRequest;
import com.example.demo.controller.dto.CancelRequest;
import com.example.demo.domain.model.Booking;
import com.example.demo.service.booking.BookingCommand;
import com.example.demo.service.booking.BookingService;
import com.example.demo.service.cancellation.CancellationResult;
import com.example.demo.service.cancellation.CancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingCommand command = BookingCommand.builder()
                .customerId(request.customerId())
                .maidId(request.maidId())
                .serviceType(request.serviceType())
                .bookingType(request.bookingType())
                .startTime(request.startTime())
                .durationMinutes(request.durationMinutes())
                .recurrencePattern(request.recurrencePattern())
                .occurrences(request.occurrences())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Booking created", bookingService.createBooking(command)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBooking(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CancellationResult>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelRequest request) {
        Long occurrenceId = request != null ? request.occurrenceId() : null;
        return ResponseEntity.ok(ApiResponse.ok(
                "Cancellation processed", cancellationService.cancelBooking(id, occurrenceId)));
    }
}
