package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.request.PeaktimeFeedbackCreateRequest;
import com.example.peakly.domain.focusSession.dto.request.SessionDisruptionsSaveRequest;
import com.example.peakly.domain.focusSession.dto.response.PeaktimeFeedbackCreateResponse;
import com.example.peakly.domain.focusSession.dto.response.SessionDisruptionsSaveResponse;

public interface PeaktimeFeedbackService {
    PeaktimeFeedbackCreateResponse createFeedback(Long userId, Long sessionId, PeaktimeFeedbackCreateRequest req);
    SessionDisruptionsSaveResponse saveDisruptions(Long userId, Long sessionId, SessionDisruptionsSaveRequest req);
}
