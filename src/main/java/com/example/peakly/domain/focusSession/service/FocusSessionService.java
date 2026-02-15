package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionStartResponse;

public interface FocusSessionService {
    FocusSessionStartResponse start(Long userId, FocusSessionStartRequest req);
}
