package com.example.peakly.domain.focusSession.dto.response;

import com.example.peakly.domain.focusSession.dto.result.PeaktimeFeedbackDTO;

public record PeaktimeFeedbackCreateResponse(
        PeaktimeFeedbackDTO feedback
) {}
