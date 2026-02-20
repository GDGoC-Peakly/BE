package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.dailySleep.repository.DailySleepLogRepository;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;
import com.example.peakly.domain.user.entity.Chronotype;
import com.example.peakly.domain.user.entity.InitialData;
import com.example.peakly.domain.user.repository.InitialDataRepository;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PeakTimePredictRequestProviderImpl implements PeakTimePredictRequestProvider {

    private final DailySleepLogRepository dailySleepLogRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    public PeakTimePredictRequest build(Long userId, LocalDate baseDate) {

        LocalDate from = baseDate.minusDays(6);

        InitialData initial = initialDataRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(
                        PeakTimePredictionErrorStatus.AI_REQUEST_SOURCE_NOT_READY));

        List<DailySleepLog> sleepLogs =
                dailySleepLogRepository.findByUser_IdAndBaseDateBetweenOrderByBaseDateAsc(
                        userId, from, baseDate);

        List<FocusSession> sessions =
                focusSessionRepository.findByUser_IdAndBaseDateBetweenAndSessionStatus(
                        userId, from, baseDate, SessionStatus.ENDED);

        Map<LocalDate, Double> sleepScoreByDate =
                sleepLogs.stream().collect(Collectors.toMap(
                        DailySleepLog::getBaseDate,
                        l -> l.getSleepScore().doubleValue()
                ));

        Map<LocalDate, Double> fatigueAvgByDate =
                sessions.stream().collect(Collectors.groupingBy(
                        FocusSession::getBaseDate,
                        Collectors.averagingInt(s -> (int) s.getFatigueLevel())
                ));

        List<PeakTimePredictRequest.RecentRecord> recent = new ArrayList<>();

        for (LocalDate d = from; !d.isAfter(baseDate); d = d.plusDays(1)) {

            Double sleepFeeling = sleepScoreByDate.get(d);
            Double fatigue = fatigueAvgByDate.get(d);

            recent.add(new PeakTimePredictRequest.RecentRecord(
                    d.toString(),
                    sleepFeeling,                   // float
                    fatigue == null ? null : fatigue.intValue()  // int
            ));
        }

        PeakTimePredictRequest.UserProfile profile =
                new PeakTimePredictRequest.UserProfile(
                        mapChronotype(initial.getChronotype()),
                        mapSensitivity(initial.getCaffeineResponsiveness()),
                        mapSensitivity(initial.getNoiseSensitivity()),
                        0.0  // optimal_hours는 float 필수
                );

        return new PeakTimePredictRequest(
                userId.toString(),
                profile,
                recent
        );
    }

    private String mapChronotype(Chronotype c) {
        return switch (c) {
            case MORNING -> "아침형";
            case AFTERNOON -> "중간형";
            case NIGHT -> "저녁형";
        };
    }

    private String mapSensitivity(byte v) {
        return switch (v) {
            case 0 -> "low";
            case 1 -> "medium";
            case 2 -> "high";
            default -> throw new IllegalArgumentException("invalid sensitivity");
        };
    }
}