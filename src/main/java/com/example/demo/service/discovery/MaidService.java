package com.example.demo.service.discovery;

import com.example.demo.domain.model.BookingOccurrence;
import com.example.demo.domain.model.Maid;
import com.example.demo.domain.model.TimeSlot;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.MaidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MaidService {

    private final MaidRepository maidRepository;
    private final BookingRepository bookingRepository;
    private final List<MaidFilter> filters;

    public Maid onboardMaid(Maid maid) {
        return maidRepository.save(maid);
    }

    public Maid getMaid(Long id) {
        return maidRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Maid not found: " + id));
    }

    public List<Maid> discoverMaids(MaidSearchCriteria criteria) {
        return maidRepository.findAll().stream()
                .filter(maid -> filters.stream().allMatch(filter -> filter.matches(maid, criteria)))
                .filter(maid -> isAvailableForSlot(maid, criteria.requestedSlot()))
                .toList();
    }

    private boolean isAvailableForSlot(Maid maid, TimeSlot requestedSlot) {
        if (requestedSlot == null) {
            return true;
        }
        if (!maid.isWithinAvailability(requestedSlot)) {
            return false;
        }
        List<BookingOccurrence> blockingOccurrences =
                bookingRepository.findBlockingOccurrencesByMaidId(maid.getId());
        return blockingOccurrences.stream()
                .noneMatch(occ -> occ.getTimeSlot().overlaps(requestedSlot));
    }
}
