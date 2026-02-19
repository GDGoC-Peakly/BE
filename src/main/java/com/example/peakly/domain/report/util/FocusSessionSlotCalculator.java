package com.example.peakly.domain.report.util;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakWindowJson;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FocusSessionSlotCalculator {

    private static final int SLOT_MINUTES = 30;

    public record TimeRange(LocalTime start, LocalTime end) {}

    private List<LocalDateTime[]> extractFocusIntervals(FocusSession session) {
        List<LocalDateTime[]> intervals = new ArrayList<>();

        LocalDateTime cursor = session.getStartedAt();
        LocalDateTime sessionEnd = session.getEndedAt();

        if (sessionEnd == null) return intervals;

        // pausedAt 기준 정렬
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
        return calcOverlapSec(sessions, slotStart, slotEnd) / 60;
    }

    public int calcTotalPeakTargetSecByRanges(List<TimeRange> ranges) {
        int total = 0;
        for (TimeRange r : ranges) {
            if (r == null) continue;
            if (r.end().isAfter(r.start())) {
                total += (int) Duration.between(r.start(), r.end()).getSeconds();
            }
        }
        return total;
    }

    public int calcTotalPeakOverlapSecByRanges(List<TimeRange> ranges, List<FocusSession> sessions) {
        int total = 0;
        for (TimeRange r : ranges) {
            if (r == null) continue;
            total += calcOverlapSec(sessions, r.start(), r.end());
        }
        return total;
    }

    private int calcOverlapSec(List<FocusSession> sessions, LocalTime slotStart, LocalTime slotEnd) {
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

                // 겹침 계산
                LocalDateTime overlapStart = iStart.isAfter(slotStartDt) ? iStart : slotStartDt;
                LocalDateTime overlapEnd = iEnd.isBefore(slotEndDt) ? iEnd : slotEndDt;

                if (overlapEnd.isAfter(overlapStart)) {
                    total += (int) Duration.between(overlapStart, overlapEnd).getSeconds();
                }

                if (!iStart.toLocalDate().equals(iEnd.toLocalDate())) {
                    LocalDateTime slotStartDt2 = iEnd.toLocalDate().atTime(slotStart);
                    LocalDateTime slotEndDt2 = iEnd.toLocalDate().atTime(slotEnd);

                    if (slotEnd.isBefore(slotStart)) {
                        slotEndDt2 = slotEndDt2.plusDays(1);
                    }

                    LocalDateTime overlapStart2 = iStart.isAfter(slotStartDt2) ? iStart : slotStartDt2;
                    LocalDateTime overlapEnd2 = iEnd.isBefore(slotEndDt2) ? iEnd : slotEndDt2;

                    if (overlapEnd2.isAfter(overlapStart2)) {
                        total += (int) Duration.between(overlapStart2, overlapEnd2).getSeconds();
                    }
                }
            }
        }

        return total;
    }

    @Deprecated
    public int calcTotalPeakOverlapSec(List<PeakWindowJson> windows, List<FocusSession> sessions) {
        int total = 0;
        for (PeakWindowJson w : windows) {
            LocalTime wStart = toLocalTime(w.hour());
            int durationMinutes = (int) Math.round(w.duration() * 60.0);
            LocalTime wEnd = wStart.plusMinutes(durationMinutes);

            for (int elapsed = 0; elapsed < durationMinutes; elapsed += SLOT_MINUTES) {
                LocalTime slotStart = wStart.plusMinutes(elapsed);
                total += calcOverlapSec(sessions, slotStart, slotStart.plusMinutes(SLOT_MINUTES));
            }
        }
        return total;
    }

    @Deprecated
    public int calcTotalPeakTargetSec(List<PeakWindowJson> windows) {
        int minutes = 0;
        for (PeakWindowJson w : windows) {
            minutes += (int) Math.round(w.duration() * 60.0);
        }
        return minutes * 60;
    }

    private LocalTime toLocalTime(Double hour) {
        int h = hour.intValue();
        int m = (Math.abs(hour - h) < 1e-9) ? 0 : 30;
        return LocalTime.of(h, m);
    }
}