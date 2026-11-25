package com.example.demo;

import com.example.demo.domain.enums.BookingStatus;
import com.example.demo.domain.enums.BookingType;
import com.example.demo.domain.enums.OccurrenceStatus;
import com.example.demo.domain.enums.PaymentMethodType;
import com.example.demo.domain.enums.PaymentStatus;
import com.example.demo.domain.enums.ServiceType;
import com.example.demo.domain.model.Booking;
import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Payment;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.cancellation.CancellationResult;
import com.example.demo.service.cancellation.CancellationService;
import com.example.demo.service.cancellation.TimeBasedCancellationPolicy;
import com.example.demo.service.payment.CardPaymentStrategy;
import com.example.demo.service.payment.MockPaymentGateway;
import com.example.demo.service.payment.PaymentService;
import com.example.demo.service.payment.UpiPaymentStrategy;
import com.example.demo.service.payment.WalletPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAndCancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;
    private CancellationService cancellationService;

    @BeforeEach
    void setUp() {
        MockPaymentGateway gateway = new MockPaymentGateway();
        paymentService = new PaymentService(
                bookingRepository,
                paymentRepository,
                gateway,
                List.of(new CardPaymentStrategy(), new UpiPaymentStrategy(), new WalletPaymentStrategy())
        );
        cancellationService = new CancellationService(
                bookingRepository,
                paymentRepository,
                gateway,
                new TimeBasedCancellationPolicy()
        );
    }

    @Test
    void paySingleOccurrenceAndCancelWithFullRefund() {
        LocalDateTime futureStart = LocalDateTime.now().plusDays(3);
        BookingOccurrence occ1 = BookingOccurrence.builder()
                .id(11L)
                .bookingId(1L)
                .maidId(5L)
                .timeSlot(new TimeSlot(futureStart, futureStart.plusHours(2)))
                .amount(BigDecimal.valueOf(500))
                .status(OccurrenceStatus.PENDING_PAYMENT)
                .build();

        BookingOccurrence occ2 = BookingOccurrence.builder()
                .id(12L)
                .bookingId(1L)
                .maidId(5L)
                .timeSlot(new TimeSlot(futureStart.plusDays(1), futureStart.plusDays(1).plusHours(2)))
                .amount(BigDecimal.valueOf(500))
                .status(OccurrenceStatus.PENDING_PAYMENT)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .customerId("cust-1")
                .maidId(5L)
                .serviceType(ServiceType.CLEANING)
                .bookingType(BookingType.RECURRING)
                .status(BookingStatus.PENDING_PAYMENT)
                .occurrences(new ArrayList<>(List.of(occ1, occ2)))
                .build();

        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(99L);
            }
            return p;
        });

        Payment payment = paymentService.processPayment(
                1L, 11L, PaymentMethodType.UPI, "idem-1", Map.of("upiId", "user@upi"));

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(OccurrenceStatus.CONFIRMED, occ1.getStatus());
        assertEquals(OccurrenceStatus.PENDING_PAYMENT, occ2.getStatus());

        when(paymentRepository.findById(99L)).thenReturn(Optional.of(payment));

        CancellationResult cancelResult = cancellationService.cancelBooking(1L, 11L);

        assertEquals(1, cancelResult.cancelledOccurrenceIds().size());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(cancelResult.totalRefundAmount()));
        assertEquals(BookingStatus.PARTIALLY_CANCELLED, cancelResult.bookingStatus());
        assertEquals(OccurrenceStatus.CANCELLED, occ1.getStatus());
    }
}
