package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.response.FocusSessionResultResponse;

public interface FocusSessionResultService {
    FocusSessionResultResponse getResult(Long userId, Long sessionId);
}
