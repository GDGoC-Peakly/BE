package com.example.peakly.domain.focusSession.controller;

import com.example.peakly.domain.focusSession.dto.response.DisruptionReasonListResponse;
import com.example.peakly.domain.focusSession.service.DisruptionReasonService;
import com.example.peakly.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/disruptions")
public class DisruptionReasonController {

    private final DisruptionReasonService disruptionReasonService;

    @GetMapping
    public ApiResponse<DisruptionReasonListResponse> getDisruptions(
            @RequestParam(required = false) Boolean activeOnly
    ) {
        DisruptionReasonListResponse res = disruptionReasonService.getDisruptionReasons(activeOnly);
        return ApiResponse.onSuccess(res);
    }
}
