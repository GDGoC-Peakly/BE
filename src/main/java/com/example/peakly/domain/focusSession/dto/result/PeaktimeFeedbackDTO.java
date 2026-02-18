package com.example.peakly.domain.focusSession.dto.result;

import java.time.LocalDateTime;

public record PeaktimeFeedbackDTO(
        Long sessionId,
        int focusScore,
        LocalDateTime recordedAt
) {}
