package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDateTime;

public record FocusSessionPauseResponse(
        Long sessionId,
        String sessionStatus,
        PauseDTO pause
) {
    public record PauseDTO(
            Long pausedId,
            LocalDateTime pausedAt
    ) {}
}
