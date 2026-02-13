package com.example.peakly.domain.user.controller;

import com.example.peakly.domain.user.dto.request.InitialDataCreateRequest;
import com.example.peakly.domain.user.dto.response.InitialDataCreateResponse;
import com.example.peakly.domain.user.service.InitialDataService;
import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class InitialDataController {

    private final InitialDataService initialDataService;

    @PostMapping("/initial-data")
    public ApiResponse<InitialDataCreateResponse> createInitialData(
            @Valid @RequestBody InitialDataCreateRequest request
    ) {
        Long userId = SecurityUtil.requireUserId();
        return ApiResponse.onSuccess(initialDataService.createInitialSetting(userId, request));
    }
}
