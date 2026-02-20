package com.example.peakly.domain.peakTimePrediction.infra;

import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;
import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictResponse;

public interface PeakTimeAiClient {
    PeakTimePredictResponse predict(PeakTimePredictRequest req);
}