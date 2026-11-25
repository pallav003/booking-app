package com.example.demo.repository.jpa;

import com.example.demo.domain.enums.Gender;
import com.example.demo.domain.enums.ServiceType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "maids")
@Getter
@Setter
@NoArgsConstructor
public class MaidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String locality;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private double rating;
    private BigDecimal hourlyRate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "maid_skills", joinColumns = @JoinColumn(name = "maid_id"))
    @Enumerated(EnumType.STRING)
    private Set<ServiceType> skills = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "maid_availability", joinColumns = @JoinColumn(name = "maid_id"))
    private List<AvailabilityWindowEmbeddable> availabilityWindows = new ArrayList<>();
}
