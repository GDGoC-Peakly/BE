package com.example.peakly.domain.focusSession.command;

public record FocusSessionStartCommand(
        Long majorCategoryId,
        Long categoryId,
        int goalDurationSec,
        int fatigueLevel,
        int caffeineIntakeLevel,
        int noiseLevel
) {}