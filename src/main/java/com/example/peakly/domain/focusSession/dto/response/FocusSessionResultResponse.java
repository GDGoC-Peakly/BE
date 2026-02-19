// src/main/java/com/example/peakly/domain/focusSession/dto/response/FocusSessionResultResponse.java
package com.example.peakly.domain.focusSession.dto.response;

import java.time.LocalDateTime;

public record FocusSessionResultResponse(
        Long sessionId,
        String sessionStatus,
        CategoryRef majorCategory,
        CategoryRef category,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer goalDurationSec,
        Integer totalFocusSec,
        Double achievementRate,
        Boolean isCountedInStats,
        Environment environment
) {
    public record CategoryRef(Long id, String name) {}

    public record Environment(
            Integer fatigueLevel,
            Integer caffeineIntakeLevel,
            Integer noiseLevel
    ) {}
}
