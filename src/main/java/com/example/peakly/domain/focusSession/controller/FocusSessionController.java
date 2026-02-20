package com.example.peakly.domain.focusSession.controller;

import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.*;
import com.example.peakly.domain.focusSession.service.FocusSessionResultService;
import com.example.peakly.domain.focusSession.service.FocusSessionService;
import com.example.peakly.domain.peakTimePrediction.dto.response.SessionPeakTimeOverlapsResponse;
import com.example.peakly.domain.peakTimePrediction.service.SessionPeakTimeOverlapsService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;
    private final FocusSessionResultService focusSessionResultService;
    private final SessionPeakTimeOverlapsService sessionPeakTimeOverlapsService;

    @PostMapping("/start")
    public ApiResponse<FocusSessionStartResponse> start(
            @Valid @RequestBody FocusSessionStartRequest req
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(focusSessionService.start(userId, req));
    }

    @PostMapping("/{sessionId}/pause")
    public ApiResponse<FocusSessionPauseResponse> pause(
            @PathVariable("sessionId") Long sessionId
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(focusSessionService.pause(userId, sessionId));
    }

    @PostMapping("/{sessionId}/resume")
    public ApiResponse<FocusSessionResumeResponse> resume(
            @PathVariable("sessionId") Long sessionId
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(focusSessionService.resume(userId, sessionId));
    }

    @PostMapping("/{sessionId}/end")
    public ApiResponse<FocusSessionEndResponse> end(
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody FocusSessionEndRequest req
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(focusSessionService.end(userId, sessionId, req));
    }

    @GetMapping("/{sessionId}/result")
    public ApiResponse<FocusSessionResultResponse> getResult(
            @PathVariable("sessionId") Long sessionId) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(focusSessionResultService.getResult(userId, sessionId));
    }

    @GetMapping("/{sessionId}/peaktime-overlaps")
    public ApiResponse<SessionPeakTimeOverlapsResponse> getPeakTimeOverlaps(
            @PathVariable("sessionId") Long sessionId
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(sessionPeakTimeOverlapsService.getSessionPeakTimeOverlaps(userId, sessionId));
    }
}
