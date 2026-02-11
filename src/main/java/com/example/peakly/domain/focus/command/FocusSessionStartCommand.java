package com.example.peakly.domain.focus.command;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FocusSessionStartCommand(
        Long majorCategoryId,
        Long categoryId,
        LocalDateTime startedAt,
        int goalDurationSec,
        int fatigueLevel,
        int caffeineIntakeLevel,
        int noiseLevel,
        LocalDate baseDate
) {
}
