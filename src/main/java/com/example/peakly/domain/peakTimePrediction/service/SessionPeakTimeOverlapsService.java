package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.dto.response.SessionPeakTimeOverlapsResponse;

public interface SessionPeakTimeOverlapsService {
    SessionPeakTimeOverlapsResponse getSessionPeakTimeOverlaps(Long userId, Long sessionId);
}