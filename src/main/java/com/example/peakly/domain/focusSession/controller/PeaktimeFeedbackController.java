package com.example.peakly.domain.focusSession.controller;

import com.example.peakly.domain.focusSession.dto.request.PeaktimeFeedbackCreateRequest;
import com.example.peakly.domain.focusSession.dto.response.PeaktimeFeedbackCreateResponse;
import com.example.peakly.domain.focusSession.service.PeaktimeFeedbackService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class PeaktimeFeedbackController {

    private final PeaktimeFeedbackService peaktimeFeedbackService;

    @PostMapping("/{sessionId}/feedback")
    public ApiResponse<PeaktimeFeedbackCreateResponse> createFeedback(
            @PathVariable Long sessionId,
            @Valid @RequestBody PeaktimeFeedbackCreateRequest req
    ) {
        Long userId = SecurityUtil.requireUserId();
        PeaktimeFeedbackCreateResponse result = peaktimeFeedbackService.createFeedback(userId, sessionId, req);
        return ApiResponse.onSuccess(result);
    }
}
