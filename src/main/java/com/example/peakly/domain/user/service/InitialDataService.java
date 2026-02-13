package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.dto.request.InitialSettingRequest;
import com.example.peakly.domain.user.dto.response.InitialSettingResponse;

public interface InitialDataService {
    InitialSettingResponse createInitialSetting(Long userId, InitialSettingRequest req);
}
