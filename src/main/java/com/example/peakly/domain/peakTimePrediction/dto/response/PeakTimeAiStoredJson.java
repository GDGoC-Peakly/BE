package com.example.peakly.domain.peakTimePrediction.dto.response;

import java.util.List;

public record PeakTimeAiStoredJson(
        List<PeakWindowJson> top_peak_times
) {}
