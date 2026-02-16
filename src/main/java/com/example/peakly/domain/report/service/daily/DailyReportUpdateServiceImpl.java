package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportUpdateServiceImpl implements DailyReportUpdateService{
    private static final int PEAK_WINDOW_MINUTES = 90;
    private static final int SLOT_MINUTES = 30;

    private final DailyReportDetailRepository dailyReportRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final PeakTimePredictionRepository peakTimePredictionRepository;
    private final FocusSessionSlotCalculator slotCalculator;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void updateReport(User user, LocalDate baseDate) {

        // 해당 날짜 완료된 세션 조회
        List<FocusSession> sessions = focusSessionRepository.findByUser_IdAndBaseDateAndSessionStatus
                (user.getId(), baseDate, SessionStatus.ENDED);

        // 통계 포함 세션만 필터링
        List<FocusSession> counted = sessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        if (counted.isEmpty()) return;

        // 달성률 계산
        int totalFocusSec  = counted.stream().mapToInt(FocusSession::getTotalFocusSec).sum();
        int totalTargetSec = counted.stream().mapToInt(FocusSession::getGoalDurationSec).sum();
        double achievementRate = totalTargetSec > 0
                ? Math.min((double) totalFocusSec / totalTargetSec * 100, 100.0)
                : 0.0;

        //적중률 계산
        double accuracyRate = calcAccuracyRate(user.getId(), counted);

        Insight insight = Insight.from(achievementRate, accuracyRate);

        // DailyReport 업데이트 (없으면 생성)
        DailyReport report = dailyReportRepository
                .findByUserIdAndReportDate(user.getId(), baseDate)
                .orElseGet(() -> DailyReport.create(user, baseDate));

        report.update(totalFocusSec, totalTargetSec, achievementRate, accuracyRate, insight);
        dailyReportRepository.save(report);
    }

    private double calcAccuracyRate(Long userId, List<FocusSession> sessions) {
        try {
            PeakTimePrediction prediction = peakTimePredictionRepository
                    .findTopByUserIdOrderByBaseDateDesc(userId)
                    .orElse(null);

            if (prediction == null) return 0.0;

            List<PeakWindowJson> windows = objectMapper.readValue(
                    prediction.getWindowJson(),
                    new TypeReference<List<PeakWindowJson>>() {});

            int totalPeakTargetSec = windows.size() * PEAK_WINDOW_MINUTES * 60;
            if (totalPeakTargetSec == 0) return 0.0;

            int totalPeakActualSec = slotCalculator.calcTotalPeakOverlapSec(windows, sessions, PEAK_WINDOW_MINUTES);

            return Math.min((double) totalPeakActualSec / totalPeakTargetSec * 100, 100.0);

        } catch (Exception e) {
            log.warn("적중률 계산 실패 userId={}", userId, e);
            return 0.0;
        }
    }
}
