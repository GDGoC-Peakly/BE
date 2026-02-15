package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDateTime;

public record FocusSessionEndResponse(
        Long sessionId,
        String sessionStatus,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer totalFocusSec,
        Integer goalDurationSec,
        Boolean isRecorded,
        Boolean isCountedInStats
) {}