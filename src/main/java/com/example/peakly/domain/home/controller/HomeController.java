package com.example.peakly.domain.home.controller;

import com.example.peakly.domain.home.dto.response.DailyPeakTimeResponse;
import com.example.peakly.domain.home.dto.response.HomeSummaryResponse;
import com.example.peakly.domain.home.service.DailyPeakTimeService;
import com.example.peakly.domain.home.service.HomeSummaryService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/daily")
public class HomeController {

    private final HomeSummaryService homeSummaryService;
    private final DailyPeakTimeService dailyPeakTimeService;

    @GetMapping("/home")
    public ApiResponse<HomeSummaryResponse> getHome(
            @RequestParam(required = false) String baseDate
    ) {
        Long userId = SecurityUtil.requireUserId();
        HomeSummaryResponse res = homeSummaryService.getHome(userId, baseDate);
        return ApiResponse.onSuccess(res);
    }

    @GetMapping("/peaktime")
    public ApiResponse<DailyPeakTimeResponse> getDailyPeakTime(
            @RequestParam(required = false) String baseDate
    ) {
        Long userId = SecurityUtil.requireUserId();
        DailyPeakTimeResponse res = dailyPeakTimeService.getDailyPeakTime(userId, baseDate);
        return ApiResponse.onSuccess(res);
    }

}
