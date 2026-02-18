package com.example.peakly.domain.focusSession.dto.result;

public record DisruptionReasonDTO(
        Long id,
        String code,
        String name,
        int sortOrder,
        boolean active
) {}
