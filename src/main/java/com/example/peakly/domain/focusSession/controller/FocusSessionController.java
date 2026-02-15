package com.example.peakly.domain.focusSession.controller;

import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionEndResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionPauseResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionResumeResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionStartResponse;
import com.example.peakly.domain.focusSession.service.FocusSessionService;
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

}
