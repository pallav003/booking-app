package com.example.demo.service.booking;

import com.example.demo.domain.enums.BookingType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BookingStrategyFactory {

    private final Map<BookingType, BookingStrategy> strategies;

    public BookingStrategyFactory(List<BookingStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(BookingStrategy::supports, Function.identity()));
    }

    public BookingStrategy getStrategy(BookingType bookingType) {
        BookingStrategy strategy = strategies.get(bookingType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported booking type: " + bookingType);
        }
        return strategy;
    }
}
