package com.example.peakly.domain.focusSession.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SessionDisruptionsSaveRequest(
        @NotEmpty(message = "disruptionReasonIds는 1개 이상이어야 합니다.")
        List<Long> disruptionReasonIds
) {}
