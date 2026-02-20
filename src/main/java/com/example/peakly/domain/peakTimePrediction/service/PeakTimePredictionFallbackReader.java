package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PeakTimePredictionFallbackReader {

    private final PeakTimePredictionRepository predictionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public PeakTimePrediction findOrFail(Long userId, LocalDate baseDate) {
        return predictionRepository.findByUser_IdAndBaseDate(userId, baseDate)
                .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PREDICTION_UPSERT_FAILED));
    }
}