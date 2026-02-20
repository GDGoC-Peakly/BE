package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;

import java.time.LocalDate;

public interface PeakTimePredictRequestProvider {
    PeakTimePredictRequest build(Long userId, LocalDate baseDate);
}
