package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.response.DisruptionReasonListResponse;

public interface DisruptionReasonService {
    DisruptionReasonListResponse getDisruptionReasons(Boolean activeOnly);
}
