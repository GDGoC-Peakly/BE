package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.dto.request.InitialSettingRequest;
import com.example.peakly.domain.user.dto.response.InitialSettingResponse;

public interface UserService {
    InitialSettingResponse saveInitialSetting(Long userId, InitialSettingRequest req);
}
