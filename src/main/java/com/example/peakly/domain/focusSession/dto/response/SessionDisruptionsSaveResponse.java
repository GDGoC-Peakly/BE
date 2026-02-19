package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SessionDisruptionsSaveResponse(
        Long sessionId,
        List<Long> reasonIds,
        LocalDateTime recordedAt
) {}
