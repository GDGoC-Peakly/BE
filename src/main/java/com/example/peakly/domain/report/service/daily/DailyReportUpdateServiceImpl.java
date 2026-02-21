package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.report.entity.DailyReport;
import com.example.peakly.domain.report.enums.Insight;
import com.example.peakly.domain.report.repository.DailyReportDetailRepository;
import com.example.peakly.domain.report.util.FocusSessionSlotCalculator;
import com.example.peakly.domain.user.entity.User;
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

    @Override
    @Transactional
    public void updateReport(User user, LocalDate baseDate) {

        List<FocusSession> sessions = focusSessionRepository
                .findByUser_IdAndBaseDateAndSessionStatus(user.getId(), baseDate, SessionStatus.ENDED);

        List<FocusSession> counted = sessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        if (counted.isEmpty()) return;

        int totalFocusSec = counted.stream().mapToInt(FocusSession::getTotalFocusSec).sum();
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

            // ✅ JSON 대신 windows 테이블(1:N) 사용
            List<PeakTimePredictionWindow> windows = prediction.getWindows();
            if (windows == null || windows.isEmpty()) return 0.0;

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

    /**
     * ✅ prediction_windows 엔티티(startMinuteOfDay, durationMinutes) → DateTimeRange 변환
     * (겹치는 구간 merge 포함)
     */
    private List<FocusSessionSlotCalculator.DateTimeRange> toPeakRanges(
            LocalDate baseDate,
            List<PeakTimePredictionWindow> windows
    ) {
        if (windows == null || windows.isEmpty()) return List.of();

        List<FocusSessionSlotCalculator.DateTimeRange> ranges = new ArrayList<>();

        for (PeakTimePredictionWindow w : windows) {
            if (w == null) continue;

            int startMin = w.getStartMinuteOfDay();
            int durationMin = w.getDurationMinutes();

            if (startMin < 0 || startMin > 1439) continue;
            if (durationMin <= 0) continue;

            LocalTime startT = LocalTime.of(startMin / 60, startMin % 60);
            LocalDateTime start = baseDate.atTime(startT);
            LocalDateTime end = start.plusMinutes(durationMin);

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
}