package com.example.peakly.domain.focusSession.dto.response;

import com.example.peakly.domain.focusSession.dto.result.DisruptionReasonDTO;

import java.util.List;

public record DisruptionReasonListResponse(
        List<DisruptionReasonDTO> disruptionReasons
) {}
