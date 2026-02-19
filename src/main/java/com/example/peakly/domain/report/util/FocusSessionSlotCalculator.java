package com.example.peakly.domain.report.util;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FocusSessionSlotCalculator {

    private static final int SLOT_MINUTES = 30;
    public record DateTimeRange(LocalDateTime start, LocalDateTime end) {}

    // slotStart~slotStart+30분 구간에서 실제 집중시간을 계산.
    public int calcActualMinInSlot(List<FocusSession> sessions, LocalDateTime slotStart) {
        LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
        return calcOverlapSec(sessions, slotStart, slotEnd) / 60;
    }

    public int calcTotalTargetSecByRanges(List<DateTimeRange> ranges) {
        int total = 0;
        for (DateTimeRange r : ranges) {
            if (r == null) continue;
            if (r.end().isAfter(r.start())) {
                total += (int) Duration.between(r.start(), r.end()).getSeconds();
            }
        }
        return total;
    }

    public int calcTotalOverlapSecByRanges(List<DateTimeRange> ranges, List<FocusSession> sessions) {
        int total = 0;
        for (DateTimeRange r : ranges) {
            if (r == null) continue;
            total += calcOverlapSec(sessions, r.start(), r.end());
        }
        return total;
    }

    //세션의 실제 집중 구간추출.
    private List<LocalDateTime[]> extractFocusIntervals(FocusSession session) {
        List<LocalDateTime[]> intervals = new ArrayList<>();

        LocalDateTime cursor = session.getStartedAt();
        LocalDateTime sessionEnd = session.getEndedAt();
        if (cursor == null || sessionEnd == null) return intervals;

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

    private int calcOverlapSec(List<FocusSession> sessions, LocalDateTime slotStartDt, LocalDateTime slotEndDt) {
        int total = 0;

        for (FocusSession session : sessions) {
            if (session.getEndedAt() == null) continue;

            List<LocalDateTime[]> focusIntervals = extractFocusIntervals(session);

            for (LocalDateTime[] interval : focusIntervals) {
                LocalDateTime iStart = interval[0];
                LocalDateTime iEnd = interval[1];

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