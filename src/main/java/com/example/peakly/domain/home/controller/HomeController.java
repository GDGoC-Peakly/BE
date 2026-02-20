package com.example.peakly.domain.home.controller;

import com.example.peakly.domain.home.dto.response.HomeSummaryResponse;
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

    @GetMapping("/home")
    public ApiResponse<HomeSummaryResponse> getHome(
            @RequestParam(required = false) String baseDate
    ) {
        Long userId = SecurityUtil.requireUserId();
        HomeSummaryResponse res = homeSummaryService.getHome(userId, baseDate);
        return ApiResponse.onSuccess(res);
    }

}
