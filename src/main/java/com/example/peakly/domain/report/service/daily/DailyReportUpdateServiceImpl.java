package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakTimeAiStoredJson;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakWindowJson;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.report.entity.DailyReport;
import com.example.peakly.domain.report.enums.Insight;
import com.example.peakly.domain.report.repository.DailyReportDetailRepository;
import com.example.peakly.domain.report.util.FocusSessionSlotCalculator;
import com.example.peakly.domain.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportUpdateServiceImpl implements DailyReportUpdateService {

    private final DailyReportDetailRepository dailyReportRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final PeakTimePredictionRepository peakTimePredictionRepository;
    private final FocusSessionSlotCalculator slotCalculator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void updateReport(User user, LocalDate baseDate) {

        List<FocusSession> sessions = focusSessionRepository
                .findByUser_IdAndBaseDateAndSessionStatus(user.getId(), baseDate, SessionStatus.ENDED);

        List<FocusSession> counted = sessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        if (counted.isEmpty()) return;

        int totalFocusSec  = counted.stream().mapToInt(FocusSession::getTotalFocusSec).sum();
        int totalTargetSec = counted.stream().mapToInt(FocusSession::getGoalDurationSec).sum();

        double achievementRate = totalTargetSec > 0
                ? Math.min((double) totalFocusSec / totalTargetSec * 100, 100.0)
                : 0.0;

        double accuracyRate = calcAccuracyRate(user.getId(), counted, baseDate);

        Insight insight = Insight.from(achievementRate, accuracyRate);

        DailyReport report = dailyReportRepository
                .findByUserIdAndReportDate(user.getId(), baseDate)
                .orElseGet(() -> DailyReport.create(user, baseDate));

        report.update(totalFocusSec, totalTargetSec, achievementRate, accuracyRate, insight);

        dailyReportRepository.save(report);
    }

    private double calcAccuracyRate(Long userId, List<FocusSession> sessions, LocalDate baseDate) {
        try {
            PeakTimePrediction prediction = peakTimePredictionRepository
                    .findTopByUserIdAndBaseDateLessThanEqualOrderByBaseDateDesc(userId, baseDate)
                    .orElse(null);

            if (prediction == null) return 0.0;

            // TODO: 엔티티에 맞춰 수정
            List<PeakWindowJson> windows = null;
//            List<PeakWindowJson> windows = parseWindows(prediction.getWindowJson());
//            if (windows.isEmpty()) return 0.0;

            List<FocusSessionSlotCalculator.DateTimeRange> peakRanges = toPeakRanges(baseDate, windows);
            if (peakRanges.isEmpty()) return 0.0;

            int totalPeakTargetSec = slotCalculator.calcTotalTargetSecByRanges(peakRanges);
            if (totalPeakTargetSec == 0) return 0.0;

            int totalPeakActualSec = slotCalculator.calcTotalOverlapSecByRanges(peakRanges, sessions);

            return Math.min((double) totalPeakActualSec / totalPeakTargetSec * 100, 100.0);

        } catch (Exception e) {
            log.warn("적중률 계산 실패 userId={}, date={}", userId, baseDate, e);
            return 0.0;
        }
    }

    private List<PeakWindowJson> parseWindows(String windowJson) throws Exception {
        String trimmed = windowJson == null ? "" : windowJson.trim();
        if (trimmed.isEmpty()) return List.of();

        if (trimmed.startsWith("[")) {
            return objectMapper.readValue(trimmed, new TypeReference<List<PeakWindowJson>>() {});
        }

        PeakTimeAiStoredJson wrapper = objectMapper.readValue(trimmed, PeakTimeAiStoredJson.class);
        return wrapper.top_peak_times() == null ? List.of() : wrapper.top_peak_times();
    }

    private List<FocusSessionSlotCalculator.DateTimeRange> toPeakRanges(LocalDate baseDate, List<PeakWindowJson> windows) {
        List<FocusSessionSlotCalculator.DateTimeRange> ranges = new ArrayList<>();

        for (PeakWindowJson w : windows) {
            if (w == null || w.hour() == null || w.duration() == null) continue;

            LocalTime startT = toLocalTime(w.hour());
            int durationMinutes = (int) Math.round(w.duration() * 60.0);
            if (durationMinutes <= 0) continue;

            LocalDateTime start = baseDate.atTime(startT);
            LocalDateTime end = start.plusMinutes(durationMinutes);
            ranges.add(new FocusSessionSlotCalculator.DateTimeRange(start, end));
        }

        if (ranges.isEmpty()) return List.of();

        ranges.sort(Comparator.comparing(FocusSessionSlotCalculator.DateTimeRange::start));

        List<FocusSessionSlotCalculator.DateTimeRange> merged = new ArrayList<>();
        FocusSessionSlotCalculator.DateTimeRange cur = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            FocusSessionSlotCalculator.DateTimeRange next = ranges.get(i);

            if (!next.start().isAfter(cur.end())) {
                LocalDateTime newEnd = next.end().isAfter(cur.end()) ? next.end() : cur.end();
                cur = new FocusSessionSlotCalculator.DateTimeRange(cur.start(), newEnd);
            } else {
                merged.add(cur);
                cur = next;
            }
        }
        merged.add(cur);

        return merged;
    }

    private LocalTime toLocalTime(Double hour) {
        int h = hour.intValue();
        int m = (Math.abs(hour - h) < 1e-9) ? 0 : 30;
        return LocalTime.of(h, m);
    }
}