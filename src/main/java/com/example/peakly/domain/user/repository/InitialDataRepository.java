package com.example.peakly.domain.user.repository;

import com.example.peakly.domain.user.entity.InitialData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InitialDataRepository extends JpaRepository<InitialData, Long> {
    boolean existsByUserId(Long userId);
}
