package com.example.peakly.domain.report.util;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FocusSessionSlotCalculator {

    private static final int SLOT_MINUTES = 30;

    // 리포트 날짜 기준 (05:00 ~ 다음날 05:00)
    private static final LocalTime REPORT_CUTOFF = ReportingDateUtil.CUTOFF_TIME;

    public record TimeRange(LocalTime start, LocalTime end) {}

    private List<LocalDateTime[]> extractFocusIntervals(FocusSession session) {
        List<LocalDateTime[]> intervals = new ArrayList<>();

        LocalDateTime cursor = session.getStartedAt();
        LocalDateTime sessionEnd = session.getEndedAt();
        if (sessionEnd == null) return intervals;

        List<SessionPause> sortedPauses = session.getPauses().stream()
                .filter(p -> p.getPausedAt() != null && p.getResumedAt() != null)
                .sorted((a, b) -> a.getPausedAt().compareTo(b.getPausedAt()))
                .toList();

        for (SessionPause pause : sortedPauses) {
            LocalDateTime pauseStart = pause.getPausedAt();
            LocalDateTime pauseEnd = pause.getResumedAt();

            if (pauseStart.isAfter(cursor)) {
                intervals.add(new LocalDateTime[]{cursor, pauseStart});
            }
            cursor = pauseEnd;
        }

        if (sessionEnd.isAfter(cursor)) {
            intervals.add(new LocalDateTime[]{cursor, sessionEnd});
        }

        return intervals;
    }

    public int calcActualMinInSlot(List<FocusSession> sessions, LocalTime slotStart) {
        LocalTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
        return calcOverlapSecLegacy(sessions, slotStart, slotEnd) / 60;
    }

    /**
     *    baseDate 기준으로 피크타임 타켓 시간 계산
     * - 피크타임을 baseDate의 리포트 날짜(05:00~익일 05:00)에 고정해서 계산
     */
    public int calcTotalPeakTargetSecByRanges(LocalDate baseDate, List<TimeRange> ranges) {
        if (baseDate == null || ranges == null || ranges.isEmpty()) return 0;

        LocalDateTime reportStart = baseDate.atTime(REPORT_CUTOFF);
        LocalDateTime reportEnd = baseDate.plusDays(1).atTime(REPORT_CUTOFF);

        long total = 0;

        for (TimeRange r : ranges) {
            if (r == null) continue;

            LocalDateTime start = toReportDateTime(baseDate, r.start());
            LocalDateTime end = toReportDateTime(baseDate, r.end());

            // 피크 구간이 자정 넘어가는 형태(예: 23:00 ~ 01:00)라면, end를 +1day
            if (!end.isAfter(start)) {
                end = end.plusDays(1);
            }

            // 리포팅 데이 범위로 클리핑
            LocalDateTime clippedStart = max(start, reportStart);
            LocalDateTime clippedEnd = min(end, reportEnd);

            if (clippedEnd.isAfter(clippedStart)) {
                total += Duration.between(clippedStart, clippedEnd).getSeconds();
            }
        }

        return safeToInt(total);
    }

    /**
     *    baseDate 기준으로 피크타임 겹치는 시간 계산
     * - 피크타임 → baseDate 리포트 날짜에 고정
     * - 세션 집중 구간과 일반적인 겹치는 구간 계산
     */
    public int calcTotalPeakOverlapSecByRanges(LocalDate baseDate, List<TimeRange> ranges, List<FocusSession> sessions) {
        if (baseDate == null || ranges == null || ranges.isEmpty() || sessions == null || sessions.isEmpty()) return 0;

        LocalDateTime reportStart = baseDate.atTime(REPORT_CUTOFF);
        LocalDateTime reportEnd = baseDate.plusDays(1).atTime(REPORT_CUTOFF);

        long total = 0;

        for (TimeRange r : ranges) {
            if (r == null) continue;

            LocalDateTime peakStart = toReportDateTime(baseDate, r.start());
            LocalDateTime peakEnd = toReportDateTime(baseDate, r.end());

            if (!peakEnd.isAfter(peakStart)) {peakEnd = peakEnd.plusDays(1);}

            LocalDateTime clippedPeakStart = max(peakStart, reportStart);
            LocalDateTime clippedPeakEnd = min(peakEnd, reportEnd);

            if (!clippedPeakEnd.isAfter(clippedPeakStart)) continue;

            // 각 세션의 실제 집중
            for (FocusSession session : sessions) {
                if (session.getEndedAt() == null) continue;

                for (LocalDateTime[] interval : extractFocusIntervals(session)) {
                    LocalDateTime focusStart = interval[0];
                    LocalDateTime focusEnd = interval[1];
                    LocalDateTime clippedFocusStart = max(focusStart, reportStart);
                    LocalDateTime clippedFocusEnd = min(focusEnd, reportEnd);
                    if (!clippedFocusEnd.isAfter(clippedFocusStart)) continue;

                    total += overlapSeconds(clippedPeakStart, clippedPeakEnd, clippedFocusStart, clippedFocusEnd);
                }
            }
        }

        return safeToInt(total);
    }

    /**
     * baseDate 리포팅 데이(05:00~익일05:00)에 LocalTime을 고정하여 LocalDateTime으로 변환
     * - time >= 05:00  => baseDate 날짜에 붙음
     * - time <  05:00  => baseDate+1 날짜에 붙음 (리포팅 데이의 "다음날 새벽" 구간이기 때문)
     */
    private LocalDateTime toReportDateTime(LocalDate baseDate, LocalTime time) {
        if (time == null) return null;
        if (!time.isBefore(REPORT_CUTOFF)) {
            return baseDate.atTime(time);
        }
        return baseDate.plusDays(1).atTime(time);
    }

    private long overlapSeconds(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        LocalDateTime start = max(aStart, bStart);
        LocalDateTime end = min(aEnd, bEnd);
        if (end.isAfter(start)) {
            return Duration.between(start, end).getSeconds();
        }
        return 0;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private int safeToInt(long v) {
        if (v <= 0) return 0;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) v;
    }

    private int calcOverlapSecLegacy(List<FocusSession> sessions, LocalTime slotStart, LocalTime slotEnd) {
        int total = 0;

        for (FocusSession session : sessions) {
            if (session.getEndedAt() == null) continue;

            List<LocalDateTime[]> focusIntervals = extractFocusIntervals(session);

            for (LocalDateTime[] interval : focusIntervals) {
                LocalDateTime iStart = interval[0];
                LocalDateTime iEnd = interval[1];
                LocalDateTime slotStartDt = iStart.toLocalDate().atTime(slotStart);
                LocalDateTime slotEndDt = iStart.toLocalDate().atTime(slotEnd);
                if (slotEnd.isBefore(slotStart)) {
                    slotEndDt = slotEndDt.plusDays(1);
                }

                LocalDateTime overlapStart = iStart.isAfter(slotStartDt) ? iStart : slotStartDt;
                LocalDateTime overlapEnd = iEnd.isBefore(slotEndDt) ? iEnd : slotEndDt;

                if (overlapEnd.isAfter(overlapStart)) {
                    total += (int) Duration.between(overlapStart, overlapEnd).getSeconds();
                }
            }
        }

        return total;
    }
}