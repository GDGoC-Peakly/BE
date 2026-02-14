package com.example.peakly.domain.dailySleep.service;

import com.example.peakly.domain.dailySleep.converter.DailySleepConverter;
import com.example.peakly.domain.dailySleep.dto.request.DailySleepLogRequest;
import com.example.peakly.domain.dailySleep.dto.request.SleepLogUpdateByTimeRequest;
import com.example.peakly.domain.dailySleep.dto.response.DailySleepLogResponse;
import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.dailySleep.repository.DailySleepLogRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.DailySleepErrorStatus;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DailySleepLogServiceImpl implements DailySleepLogService{

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime BASE_DATE_CUTOFF = LocalTime.of(5, 0);
    private static final LocalTime INVALID_START = LocalTime.of(4, 55);

    private final DailySleepLogRepository sleepLogRepository;
    private final DailySleepConverter sleepConverter;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DailySleepLogResponse saveSleepLog(Long userId, DailySleepLogRequest request){

        LocalDateTime now = LocalDateTime.now(KST);
        validateOperationTime(now.toLocalTime());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        LocalDate baseDate = resolveBaseDate(now);

        if (LocalTime.now().isBefore(LocalTime.of(5, 0))) {
            baseDate = baseDate.minusDays(1);
        }

        if (sleepLogRepository.findByUserAndBaseDate(user, baseDate).isPresent()) {
            throw new GeneralException(DailySleepErrorStatus.SLEEP_ALREADY_EXISTS);
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
                .orElseThrow(() -> new GeneralException(DailySleepErrorStatus.SLEEP_NOT_FOUND));

        return sleepConverter.toResponse(sleepLog);
    }

    @Override
    @Transactional
    public DailySleepLogResponse updateSleepLog(
            Long userId,
            SleepLogUpdateByTimeRequest request,
            LocalDate baseDate){

        LocalDateTime now = LocalDateTime.now(KST);
        validateOperationTime(now.toLocalTime());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        DailySleepLog sleepLog = sleepLogRepository.findByUserAndBaseDate(user, baseDate)
                .orElseThrow(() -> new GeneralException(DailySleepErrorStatus.SLEEP_NOT_FOUND));

        sleepConverter.updateEntity(sleepLog, request);

        return sleepConverter.toResponse(sleepLog);
    }

    private LocalDate resolveBaseDate(LocalDateTime now) {
        LocalDate baseDate = now.toLocalDate();
        if (now.toLocalTime().isBefore(BASE_DATE_CUTOFF)) {
            baseDate = baseDate.minusDays(1);
        }
        return baseDate;
    }

    private void validateOperationTime(LocalTime nowTime) {
        if (nowTime.isAfter(INVALID_START) && nowTime.isBefore(BASE_DATE_CUTOFF)) {
            throw new GeneralException(DailySleepErrorStatus.INVALID_OPERATION_TIME);
        }
    }
}
