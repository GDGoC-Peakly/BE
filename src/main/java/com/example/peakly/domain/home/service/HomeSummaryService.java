package com.example.peakly.domain.home.service;

import com.example.peakly.domain.home.dto.response.HomeSummaryResponse;

public interface HomeSummaryService {
    HomeSummaryResponse getHome(Long userId, String baseDateRaw);
}
