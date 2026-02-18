package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.request.PeaktimeFeedbackCreateRequest;
import com.example.peakly.domain.focusSession.dto.response.PeaktimeFeedbackCreateResponse;

public interface PeaktimeFeedbackService {
    PeaktimeFeedbackCreateResponse createFeedback(Long userId, Long sessionId, PeaktimeFeedbackCreateRequest req);
}
