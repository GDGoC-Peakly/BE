package com.example.peakly.domain.peakTimePrediction.repository;

import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeakTimePredictionWindowRepository extends JpaRepository<PeakTimePredictionWindow, Long> {

    List<PeakTimePredictionWindow> findAllByPrediction_IdOrderByStartMinuteOfDayAsc(Long predictionId);

    void deleteAllByPrediction_Id(Long predictionId);
}