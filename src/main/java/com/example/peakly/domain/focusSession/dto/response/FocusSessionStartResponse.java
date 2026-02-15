package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FocusSessionStartResponse(
        Long sessionId,
        String sessionStatus,
        LocalDateTime startedAt,
        LocalDate baseDate,
        Integer goalDurationSec,
        LocalDateTime expectedEndAt
) {}