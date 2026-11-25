package com.example.demo.repository;

import com.example.demo.domain.model.Maid;

import java.util.List;
import java.util.Optional;

public interface MaidRepository {
    Maid save(Maid maid);

    Optional<Maid> findById(Long id);

    List<Maid> findAll();
}
