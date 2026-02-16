package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionEndResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionPauseResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionResumeResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionStartResponse;

public interface FocusSessionService {

    FocusSessionStartResponse start(Long userId, FocusSessionStartRequest req);

    FocusSessionPauseResponse pause(Long userId, Long sessionId);

    FocusSessionResumeResponse resume(Long userId, Long sessionId);

    FocusSessionEndResponse end(Long userId, Long sessionId, FocusSessionEndRequest req);
}
