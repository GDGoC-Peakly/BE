package com.example.peakly.domain.peakTimePrediction.controller;

import com.example.peakly.domain.peakTimePrediction.dto.response.PeakTimePredictionRefreshResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.service.PeakTimePredictionEnsureService;
import com.example.peakly.domain.peakTimePrediction.util.DailyQueryParser;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/daily")
public class PeakTimePredictionController {

    private final PeakTimePredictionEnsureService ensureService;

    @PostMapping("/peaktime/refresh")
    public ApiResponse<PeakTimePredictionRefreshResponse> refresh(
            @RequestParam(value = "baseDate", required = false) String baseDateStr
    ) {
        Long userId = SecurityUtil.requireUserId();
        LocalDate baseDate = DailyQueryParser.parseBaseDateOrToday(baseDateStr);

        return ApiResponse.onSuccess(
                ensureService.refreshResponse(userId, baseDate)
        );
    }
}