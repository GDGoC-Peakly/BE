package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.report.dto.response.DailyReportDetailResponse;

import java.time.LocalDate;

public interface DailyReportDetailService {
    DailyReportDetailResponse getDailyReport(Long userId, LocalDate date);
}
