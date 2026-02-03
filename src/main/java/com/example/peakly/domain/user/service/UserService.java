package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.dto.request.InitialSettingRequest;
import com.example.peakly.domain.user.dto.response.InitialSettingResponse;
import jakarta.validation.constraints.NotNull;

public interface UserService {
    InitialSettingResponse saveInitialSetting(
            @NotNull Long userId,
            @NotNull InitialSettingRequest req);
}
