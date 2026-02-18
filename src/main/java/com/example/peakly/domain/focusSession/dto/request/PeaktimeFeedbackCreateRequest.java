package com.example.peakly.domain.focusSession.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PeaktimeFeedbackCreateRequest(
        @NotNull(message = "focusScore는 필수입니다.")
        @Min(value = 1, message = "focusScore는 1~5 범위여야 합니다.")
        @Max(value = 5, message = "focusScore는 1~5 범위여야 합니다.")
        Integer focusScore
) {}
