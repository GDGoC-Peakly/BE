package com.example.peakly.domain.dailySleep.service;

import com.example.peakly.domain.dailySleep.converter.DailySleepConverter;
import com.example.peakly.domain.dailySleep.dto.request.DailySleepLogRequest;
import com.example.peakly.domain.dailySleep.dto.request.SleepLogUpdateByTimeRequest;
import com.example.peakly.domain.dailySleep.dto.response.DailySleepLogResponse;
import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.dailySleep.repository.DailySleepLogRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.DailySleepErrorCode;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DailySleepLogServiceImpl implements DailySleepLogService{

    private final DailySleepLogRepository sleepLogRepository;
    private final DailySleepConverter sleepConverter;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DailySleepLogResponse saveSleepLog(Long userId, DailySleepLogRequest request){

        validateOperationTime();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        LocalDate baseDate = LocalDate.now();
        if (LocalTime.now().isBefore(LocalTime.of(5, 0))) {
            baseDate = baseDate.minusDays(1);
        }

        if (sleepLogRepository.findByUserAndBaseDate(user, baseDate).isPresent()) {
            throw new GeneralException(DailySleepErrorCode.SLEEP_ALREADY_EXISTS);
        }
        DailySleepLog sleepLog = sleepConverter.toEntity(request, user, baseDate);
        DailySleepLog savedLog = sleepLogRepository.save(sleepLog);

        return sleepConverter.toResponse(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public DailySleepLogResponse getSleepLog(Long userId, LocalDate baseDate){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        DailySleepLog sleepLog = sleepLogRepository.findByUserAndBaseDate(user, baseDate)
                .orElseThrow(() -> new GeneralException(DailySleepErrorCode.SLEEP_NOT_FOUND));

        return sleepConverter.toResponse(sleepLog);
    }

    @Override
    @Transactional
    public DailySleepLogResponse updateSleepLog(
            Long userId,
            SleepLogUpdateByTimeRequest request,
            LocalDate baseDate){

        validateOperationTime();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        DailySleepLog sleepLog = sleepLogRepository.findByUserAndBaseDate(user, baseDate)
                .orElseThrow(() -> new GeneralException(DailySleepErrorCode.SLEEP_NOT_FOUND));

        sleepConverter.updateEntity(sleepLog, request);

        return sleepConverter.toResponse(sleepLog);
    }

    private void validateOperationTime() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(4, 55)) && now.isBefore(LocalTime.of(5, 0))) {
            throw new GeneralException(DailySleepErrorCode.INVALID_OPERATION_TIME);
        }
    }
}
