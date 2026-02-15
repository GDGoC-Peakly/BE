package com.example.peakly.domain.dailySleep.converter;

import com.example.peakly.domain.dailySleep.dto.request.DailySleepLogRequest;
import com.example.peakly.domain.dailySleep.dto.request.SleepLogUpdateByTimeRequest;
import com.example.peakly.domain.dailySleep.dto.response.DailySleepLogResponse;
import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DailySleepConverter {

    public DailySleepLog toEntity(DailySleepLogRequest request, User user, LocalDate baseDate){
        return DailySleepLog.builder()
                .user(user)
                .baseDate(baseDate)
                .bedTime(request.bedTime())
                .wakeTime(request.wakeTime())
                .sleepScore(request.sleepScore())
                .sleepDurationMin(calculateDuration(request.bedTime(),request.wakeTime()))
                .build();
    }

    public void updateEntity(DailySleepLog existingLog, SleepLogUpdateByTimeRequest request) {
        existingLog.updateLog(
                request.bedTime(),
                request.wakeTime(),
                calculateDuration(request.bedTime(), request.wakeTime()), // 수정된 시간 재계산
                request.sleepScore()
        );
    }

    // 수면시간 계산
    private Integer calculateDuration(LocalTime bedTime, LocalTime wakeTime){
        Duration duration = Duration.between(bedTime,wakeTime);
        if (duration.isNegative()){
            duration = duration.plusDays(1);
        }
        return (int) duration.toMinutes();
    }

    public DailySleepLogResponse toResponse(DailySleepLog entity) {
        int hours = entity.getSleepDurationMin() / 60;
        int mins = entity.getSleepDurationMin() % 60;
        String durationDisplay = String.format("%dh %dm", hours, mins);

        return new DailySleepLogResponse(
                entity.getId(),
                entity.getBaseDate(),
                entity.getBedTime(),
                entity.getWakeTime(),
                entity.getSleepScore(),
                durationDisplay
        );
    }
}
