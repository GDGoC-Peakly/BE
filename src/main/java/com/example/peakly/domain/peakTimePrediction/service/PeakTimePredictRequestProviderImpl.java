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

    private static final int WINDOW_DAYS = 7;

    private static final double DEFAULT_SLEEP_FEELING = 3.5;
    private static final int DEFAULT_FATIGUE_LEVEL = 3;

    private final DailySleepLogRepository dailySleepLogRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    public PeakTimePredictRequest build(Long userId, LocalDate baseDate) {

        LocalDate from = baseDate.minusDays(WINDOW_DAYS - 1);

        InitialData initial = initialDataRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(
                        PeakTimePredictionErrorStatus.AI_REQUEST_SOURCE_NOT_READY));

        List<DailySleepLog> sleepLogs =
                dailySleepLogRepository.findByUser_IdAndBaseDateBetweenOrderByBaseDateAsc(
                        userId, from, baseDate);

        Map<LocalDate, Double> sleepScoreByDate =
                sleepLogs.stream().collect(Collectors.toMap(
                        DailySleepLog::getBaseDate,
                        l -> l.getSleepScore().doubleValue(),
                        (a, b) -> b
                ));

        List<FocusSession> sessions =
                focusSessionRepository.findByUser_IdAndBaseDateBetweenAndSessionStatus(
                        userId, from, baseDate, SessionStatus.ENDED);

        Map<LocalDate, Double> fatigueAvgByDate =
                sessions.stream().collect(Collectors.groupingBy(
                        FocusSession::getBaseDate,
                        Collectors.averagingInt(s -> (int) s.getFatigueLevel()) // byte -> int
                ));

        List<PeakTimePredictRequest.RecentRecord> recent = new ArrayList<>();

        for (LocalDate d = from; !d.isAfter(baseDate); d = d.plusDays(1)) {
            Double sleepFeeling = sleepScoreByDate.get(d);
            Double fatigueAvg = fatigueAvgByDate.get(d);

            if (sleepFeeling == null || fatigueAvg == null) continue;

            recent.add(new PeakTimePredictRequest.RecentRecord(
                    d.toString(),
                    sleepFeeling,
                    fatigueAvg.intValue()
            ));
        }

        // 피크타임은 무조건 줘야 하므로 AI가 0~1개 데이터에서 터지지 않도록 최소 2개 보강
        // TODO: 추후 AI가 결측/빈 데이터 방어를 구현하면, 이 보강 로직 제거하고 폴백 정책으로 전환.
        ensureAtLeastTwoRecords(recent, baseDate);

        PeakTimePredictRequest.UserProfile profile =
                new PeakTimePredictRequest.UserProfile(
                        mapChronotype(initial.getChronotype()),
                        mapSensitivity(initial.getCaffeineResponsiveness()),
                        mapSensitivity(initial.getNoiseSensitivity()),
                        0.0
                );

        return new PeakTimePredictRequest(
                userId.toString(),
                profile,
                recent
        );
    }

    private void ensureAtLeastTwoRecords(
            List<PeakTimePredictRequest.RecentRecord> recent,
            LocalDate baseDate
    ) {
        if (recent.size() >= 2) return;

        if (recent.isEmpty()) {
            recent.add(new PeakTimePredictRequest.RecentRecord(
                    baseDate.minusDays(1).toString(),
                    DEFAULT_SLEEP_FEELING,
                    DEFAULT_FATIGUE_LEVEL
            ));
            recent.add(new PeakTimePredictRequest.RecentRecord(
                    baseDate.toString(),
                    DEFAULT_SLEEP_FEELING,
                    DEFAULT_FATIGUE_LEVEL
            ));
            return;
        }

        PeakTimePredictRequest.RecentRecord only = recent.get(0);

        double sleepFeeling = (only.sleep_feeling() != null) ? only.sleep_feeling() : DEFAULT_SLEEP_FEELING;
        int fatigueLevel = (only.fatigue_level() != null) ? only.fatigue_level() : DEFAULT_FATIGUE_LEVEL;

        LocalDate onlyDate;
        try {
            onlyDate = LocalDate.parse(only.date());
        } catch (Exception e) {
            onlyDate = baseDate;
        }

        LocalDate d0 = baseDate.minusDays(1);
        LocalDate d1 = baseDate;

        if (onlyDate.equals(d0)) {
            recent.add(new PeakTimePredictRequest.RecentRecord(
                    d1.toString(),
                    sleepFeeling,
                    fatigueLevel
            ));
            return;
        }

        if (onlyDate.equals(d1)) {
            recent.add(0, new PeakTimePredictRequest.RecentRecord(
                    d0.toString(),
                    sleepFeeling,
                    fatigueLevel
            ));
            return;
        }

        LocalDate synthetic = d1;
        while (synthetic.equals(onlyDate)) {
            synthetic = synthetic.minusDays(1);
        }

        recent.add(new PeakTimePredictRequest.RecentRecord(
                synthetic.toString(),
                sleepFeeling,
                fatigueLevel
        ));
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