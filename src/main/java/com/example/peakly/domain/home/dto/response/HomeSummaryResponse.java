package com.example.peakly.domain.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomeSummaryResponse(
        String baseDate,                 // YYYY-MM-DD
        LocalDateTime now,               // ISO-8601
        PeakTimeDTO peaktime,
        SummaryDTO summary
) {
    public record PeakTimeDTO(
            List<PeakWindowDTO> windows,
            boolean isNowInPeakTime,
            PeakWindowDTO currentWindow,
            String modelVersion,
            LocalDateTime computedAt
    ) {}

    public record PeakWindowDTO(
            int rank,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            double scoreRaw,
            double score01
    ) {}

    public record SummaryDTO(
            int totalFocusSec,
            SleepDTO sleep
    ) {}

    public record SleepDTO(
            int sleepSec,
            double sleepScore
    ) {}
}