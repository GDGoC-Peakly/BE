package com.example.peakly.domain.peakTimePrediction.repository;

import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PeakTimePredictionRepository extends JpaRepository<PeakTimePrediction, Long> {

    // 유저의 가장 최근 예측값 조회
    //Optional<PeakTimePrediction> findTopByUserIdOrderByBaseDateDesc(Long userId);

    @EntityGraph(attributePaths = "windows")
    Optional<PeakTimePrediction> findTopByUserIdAndBaseDateLessThanEqualOrderByBaseDateDesc(Long userId, LocalDate baseDate);

    @EntityGraph(attributePaths = "windows")
    Optional<PeakTimePrediction> findByUser_IdAndBaseDate(Long userId, LocalDate baseDate);
}

