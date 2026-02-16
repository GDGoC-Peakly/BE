package com.example.peakly.domain.report.controller;

import com.example.peakly.domain.report.dto.response.DailyReportDetailResponse;
import com.example.peakly.domain.report.service.daily.DailyReportDetailService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports/daily")
@Tag(name = "Daily_Report", description = "일간 리포트 조회 API")
public class DailyReportDetailController {

    private final DailyReportDetailService dailyService;

    @Operation(
            summary = "일간 상세 리포트 조회",
            description = "시간대별 그래프 데이터는 당일에, 특정 날짜의 달성률 및 적중률은 다음날에 조회합니다."
    )
    @GetMapping("/{date}")
    public ApiResponse<DailyReportDetailResponse> getDailyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        Long userId = SecurityUtil.requireUserId();
        DailyReportDetailResponse response = dailyService.getDailyReport(userId, date);

        return ApiResponse.onSuccess(response);
    }

}
