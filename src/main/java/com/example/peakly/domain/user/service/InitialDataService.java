package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.dto.request.InitialDataCreateRequest;
import com.example.peakly.domain.user.dto.response.InitialDataCreateResponse;

public interface InitialDataService {
    InitialDataCreateResponse createInitialSetting(Long userId, InitialDataCreateRequest req);
}
