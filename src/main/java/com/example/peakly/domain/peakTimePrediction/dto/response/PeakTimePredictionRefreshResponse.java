package com.example.peakly.domain.peakTimePrediction.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PeakTimePredictionRefreshResponse(
        Long id,
        LocalDate baseDate,
        String modelVersion,
        LocalDateTime computedAt,
        List<WindowDTO> windows
) {
    public record WindowDTO(
            int rank,
            LocalDateTime startAt,
            LocalDateTime endAt,
            double scoreRaw,
            double score01
    ) {}
}