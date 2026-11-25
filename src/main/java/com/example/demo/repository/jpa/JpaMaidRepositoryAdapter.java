package com.example.demo.repository.jpa;

import com.example.demo.domain.model.AvailabilityWindow;
import com.example.demo.domain.model.Maid;
import com.example.demo.repository.MaidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaMaidRepositoryAdapter implements MaidRepository {

    private final MaidJpaRepository jpaRepository;

    @Override
    public Maid save(Maid maid) {
        MaidEntity entity = toEntity(maid);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Maid> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Maid> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private MaidEntity toEntity(Maid maid) {
        MaidEntity entity = new MaidEntity();
        entity.setId(maid.getId());
        entity.setName(maid.getName());
        entity.setLocality(maid.getLocality());
        entity.setGender(maid.getGender());
        entity.setRating(maid.getRating());
        entity.setHourlyRate(maid.getHourlyRate());
        entity.setSkills(new HashSet<>(maid.getSkills()));
        entity.setAvailabilityWindows(maid.getAvailabilityWindows().stream()
                .map(w -> new AvailabilityWindowEmbeddable(w.dayOfWeek(), w.startTime(), w.endTime()))
                .toList());
        return entity;
    }

    private Maid toDomain(MaidEntity entity) {
        return Maid.builder()
                .id(entity.getId())
                .name(entity.getName())
                .locality(entity.getLocality())
                .gender(entity.getGender())
                .rating(entity.getRating())
                .hourlyRate(entity.getHourlyRate())
                .skills(new HashSet<>(entity.getSkills()))
                .availabilityWindows(entity.getAvailabilityWindows().stream()
                        .map(w -> new AvailabilityWindow(w.getDayOfWeek(), w.getStartTime(), w.getEndTime()))
                        .toList())
                .build();
    }
}
