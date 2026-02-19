package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.user.entity.User;

import java.time.LocalDate;

public interface DailyReportUpdateService {
    void updateReport(User user, LocalDate baseDate);
}
