package com.example.peakly.domain.home.service;

import com.example.peakly.domain.home.dto.response.DailyPeakTimeResponse;

public interface DailyPeakTimeService {
    DailyPeakTimeResponse getDailyPeakTime(Long userId, String baseDate);
}