package com.example.peakly.domain.report.util;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakWindowJson;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Component
public class FocusSessionSlotCalculator {

    private static final int SLOT_MINUTES = 30;

    // 슬롯 시작 시간 기준 실제 집중 시간(분) 계산
    public int calcActualMinInSlot(List<FocusSession> sessions, LocalTime slotStart) {
        LocalTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
        return calcOverlapSec(sessions, slotStart, slotEnd) / 60;
    }

    // 피크 윈도우 전체의 겹치는 시간(초) 합산
    public int calcTotalPeakOverlapSec(
            List<PeakWindowJson> windows, List<FocusSession> sessions, int peakWindowMinutes) {
        int total = 0;
        for (PeakWindowJson window : windows) {
            for (int elapsed = 0; elapsed < peakWindowMinutes; elapsed += SLOT_MINUTES) {
                LocalTime slotStart = LocalTime.of(window.hour(), 0).plusMinutes(elapsed);
                LocalTime slotEnd   = slotStart.plusMinutes(SLOT_MINUTES);
                total += calcOverlapSec(sessions, slotStart, slotEnd);
            }
        }
        return total;
    }

    // 슬롯과 세션의 겹치는 시간(초) 계산
    private int calcOverlapSec(
            List<FocusSession> sessions, LocalTime slotStart, LocalTime slotEnd) {
        int total = 0;
        for (FocusSession session : sessions) {
            if (session.getEndedAt() == null) continue;

            LocalTime sStart = session.getStartedAt().toLocalTime();
            LocalTime sEnd   = session.getEndedAt().toLocalTime();

            LocalTime overlapStart = sStart.isAfter(slotStart) ? sStart : slotStart;
            LocalTime overlapEnd   = sEnd.isBefore(slotEnd)    ? sEnd   : slotEnd;

            if (overlapEnd.isAfter(overlapStart)) {
                total += (int) Duration.between(overlapStart, overlapEnd).getSeconds();
            }
        }
        return total;
    }
}