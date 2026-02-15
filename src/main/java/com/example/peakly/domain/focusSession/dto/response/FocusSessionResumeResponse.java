package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDateTime;

public record FocusSessionResumeResponse(
        Long sessionId,
        String sessionStatus,
        Integer totalFocusSec,
        LocalDateTime expectedEndAt,
        PauseDTO pause
) {
    public record PauseDTO(
            Long pauseId,
            LocalDateTime pausedAt,
            LocalDateTime resumedAt,
            Integer pauseSec
    ) {}
}