package com.example.peakly.domain.focusSession.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FocusSessionEndRequest(
        @NotNull Boolean isRecorded,
        @NotNull @Min(0) Integer clientTotalFocusTimeSec
) {
}
