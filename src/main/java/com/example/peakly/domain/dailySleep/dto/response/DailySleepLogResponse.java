package com.example.peakly.domain.dailySleep.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record DailySleepLogResponse(
        Long id,
        LocalDate baseDate,
        LocalTime bedTime,
        LocalTime wakeTime,
        Integer sleepScore,
        String durationDisplay
) {
}
