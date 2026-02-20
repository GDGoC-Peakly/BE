package com.example.peakly.domain.peakTimePrediction.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SessionPeakTimeOverlapsResponse(
        LocalDate baseDate,
        SessionDTO session,
        List<WindowDTO> windows,
        List<OverlapDTO> overlaps,
        String modelVersion,
        LocalDateTime computedAt
) {
    public record SessionDTO(
            Long sessionId,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            int totalFocusSec
    ) {}

    public record WindowDTO(
            int rank,
            LocalDateTime startAt,
            LocalDateTime endAt,
            double scoreRaw,
            double score01
    ) {}

    public record OverlapDTO(
            int rank,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {}
}