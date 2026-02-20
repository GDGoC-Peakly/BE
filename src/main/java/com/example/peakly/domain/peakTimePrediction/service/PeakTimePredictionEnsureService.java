package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.dto.response.PeakTimePredictionRefreshResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;

import java.time.LocalDate;

public interface PeakTimePredictionEnsureService {
    PeakTimePrediction ensure(Long userId, LocalDate baseDate);
    PeakTimePrediction refresh(Long userId, LocalDate baseDate);
    PeakTimePredictionRefreshResponse refreshResponse(Long userId, LocalDate baseDate);
}