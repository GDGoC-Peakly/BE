package com.example.peakly.domain.dailySleep.service;

import com.example.peakly.domain.dailySleep.dto.request.DailySleepLogRequest;
import com.example.peakly.domain.dailySleep.dto.request.SleepLogUpdateByTimeRequest;
import com.example.peakly.domain.dailySleep.dto.response.DailySleepLogResponse;
import com.example.peakly.domain.user.entity.User;

import java.time.LocalDate;

public interface DailySleepLogService {
    DailySleepLogResponse saveSleepLog(Long userId, DailySleepLogRequest request);

    DailySleepLogResponse getSleepLog(Long userId, LocalDate baseDate);

    DailySleepLogResponse updateSleepLog(Long userId, SleepLogUpdateByTimeRequest request, LocalDate baseDate);
}
