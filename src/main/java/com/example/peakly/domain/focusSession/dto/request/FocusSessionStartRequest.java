package com.example.peakly.domain.focusSession.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FocusSessionStartRequest(
    @NotNull Long majorCategoryId,
    Long categoryId,

    @NotNull @Min(1) @Max(86400) Integer goalDurationSec,

    @NotNull @Min(1) @Max(5) Integer fatigueLevel,
    @NotNull @Min(0) @Max(2) Integer caffeineIntakeLevel,
    @NotNull @Min(0) @Max(2) Integer noiseLevel
) {}