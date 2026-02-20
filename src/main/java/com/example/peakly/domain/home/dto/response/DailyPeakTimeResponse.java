package com.example.peakly.domain.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record DailyPeakTimeResponse(
        String baseDate,
        List<WindowDTO> windows,
        String modelVersion,
        LocalDateTime computedAt
) {
    public record WindowDTO(
            int rank,
            LocalDateTime startAt,
            LocalDateTime endAt,
            double scoreRaw,
            double score01
    ) {}
}