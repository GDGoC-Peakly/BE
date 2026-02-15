package com.example.peakly.domain.dailySleep.controller;

import com.example.peakly.domain.dailySleep.dto.request.DailySleepLogRequest;
import com.example.peakly.domain.dailySleep.dto.request.SleepLogUpdateByTimeRequest;
import com.example.peakly.domain.dailySleep.dto.response.DailySleepLogResponse;
import com.example.peakly.domain.dailySleep.service.DailySleepLogService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/daily/check-in")
@Tag(name = "Daily_Check-in", description = "데일리 체크인 입력/조회/수정 API")
public class DailySleepLogController {
    private final DailySleepLogService sleepLogService;

    @Operation(
            summary = "데일리 체크인 작성",
            description = "오늘 날짜의 숙면시간과 수면의 질을 기입합니다."
    )
    @PostMapping
    public ApiResponse<DailySleepLogResponse> saveSleepLog(
            @RequestBody @Valid DailySleepLogRequest request) {

        Long userId = SecurityUtil.requireUserId();
        DailySleepLogResponse response = sleepLogService.saveSleepLog(userId, request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "데일리 체크인 조회",
            description = "숙면시간과 수면의 질을 기입한 내용을 조회합니다."
    )
    @GetMapping("/{baseDate}")
    public ApiResponse<DailySleepLogResponse> getSleepLog(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate) {

        Long userId = SecurityUtil.requireUserId();
        DailySleepLogResponse response = sleepLogService.getSleepLog(userId, baseDate);
        return ApiResponse.onSuccess(response);
    }

    @Operation(
            summary = "데일리 체크인 수정",
            description = "숙면시간과 수면의 질을 기입한 내용을 수정합니다."
    )
    @PatchMapping("/{baseDate}")
    public ApiResponse<DailySleepLogResponse> updateSleepLog(
            @RequestBody @Valid SleepLogUpdateByTimeRequest request,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
            ){
        Long userId = SecurityUtil.requireUserId();
        DailySleepLogResponse response = sleepLogService.updateSleepLog(userId, request, baseDate);
        return ApiResponse.onSuccess(response);
    }

}
