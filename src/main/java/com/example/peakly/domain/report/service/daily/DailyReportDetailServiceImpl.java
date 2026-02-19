package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakTimeAiStoredJson;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakWindowJson;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.report.converter.ReportConverter;
import com.example.peakly.domain.report.dto.response.DailyReportDetailResponse;
import com.example.peakly.domain.report.entity.DailyReport;
import com.example.peakly.domain.report.repository.DailyReportDetailRepository;
import com.example.peakly.domain.report.util.FocusSessionSlotCalculator;
import com.example.peakly.global.apiPayload.code.status.DailyReportErrorStatus;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyReportDetailServiceImpl implements DailyReportDetailService {

    private static final int SLOT_MINUTES = 30;

    private final DailyReportDetailRepository dailyReportRepository;
    private final PeakTimePredictionRepository peakTimeRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final FocusSessionSlotCalculator slotCalculator;
    private final ReportConverter reportConverter;
    private final ObjectMapper objectMapper;

    @Override
    public DailyReportDetailResponse getDailyReport(Long userId, LocalDate date) {

        LocalDate slotDate = date;
        LocalDate statsDate = date.minusDays(1);

        PeakTimePrediction prediction = peakTimeRepository
                .findTopByUserIdAndBaseDateLessThanEqualOrderByBaseDateDesc(userId, slotDate)
                .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PREDICTION_NOT_FOUND));

        List<FocusSession> slotSessions = focusSessionRepository
                .findByUser_IdAndBaseDateAndSessionStatus(userId, slotDate, SessionStatus.ENDED);

        List<FocusSession> counted = slotSessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        List<PeakWindowJson> windows = parseWindows(prediction.getWindowJson());
        List<FocusSessionSlotCalculator.TimeRange> peakRanges = mergePeakWindows(windows);
        List<FocusSessionSlotCalculator.TimeRange> sessionRanges = extractSessionRanges(counted);

        // 피크 슬롯 생성
        Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> peakMap = new LinkedHashMap<>();
        putSlots(peakMap, peakRanges, counted, true);

        // 비피크 슬롯 생성 - 세션 범위에서 만들되, 피크에 속한 슬롯은 제외
        Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> otherMap = new LinkedHashMap<>();
        putSlotsExcludeKeys(otherMap, sessionRanges, counted, peakMap.keySet());

        List<DailyReportDetailResponse.TimeSlotDto> peakTimeSlots = new ArrayList<>(peakMap.values());
        List<DailyReportDetailResponse.TimeSlotDto> otherTimeSlots = new ArrayList<>(otherMap.values());

        int peakActualMin = peakTimeSlots.stream().mapToInt(ts -> nvl(ts.actualMinutes())).sum();
        int peakTargetMin = peakTimeSlots.stream().mapToInt(ts -> nvl(ts.targetMinutes())).sum();

        int otherActualMin = otherTimeSlots.stream().mapToInt(ts -> nvl(ts.actualMinutes())).sum();
        int otherTargetMin = otherTimeSlots.stream().mapToInt(ts -> nvl(ts.targetMinutes())).sum(); // 보통 0

        DailyReport statsReport = dailyReportRepository
                .findByUserIdAndReportDate(userId, statsDate)
                .orElse(null);

        return reportConverter.toDetailResponse(
                slotDate,
                slotDate.getDayOfWeek().name(),
                statsDate,
                statsReport,
                peakTimeSlots,
                otherTimeSlots,
                peakActualMin,
                peakTargetMin,
                otherActualMin,
                otherTargetMin
        );
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private void putSlots(
            Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> map,
            List<FocusSessionSlotCalculator.TimeRange> ranges,
            List<FocusSession> sessions,
            boolean isPeak
    ) {
        if (ranges == null || ranges.isEmpty()) return;

        int targetMin = isPeak ? SLOT_MINUTES : 0;

        for (FocusSessionSlotCalculator.TimeRange r : ranges) {
            if (r == null) continue;

            LocalTime t = r.start();
            while (t.isBefore(r.end())) {
                int actualMin = slotCalculator.calcActualMinInSlot(sessions, t);
                map.putIfAbsent(t, reportConverter.toTimeSlotDto(t, actualMin, targetMin));
                t = t.plusMinutes(SLOT_MINUTES);
            }
        }
    }

    private void putSlotsExcludeKeys(
            Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> map,
            List<FocusSessionSlotCalculator.TimeRange> ranges,
            List<FocusSession> sessions,
            Set<LocalTime> exclude
    ) {
        if (ranges == null || ranges.isEmpty()) return;

        for (FocusSessionSlotCalculator.TimeRange r : ranges) {
            if (r == null) continue;

            LocalTime t = r.start();
            while (t.isBefore(r.end())) {
                if (!exclude.contains(t)) {
                    int actualMin = slotCalculator.calcActualMinInSlot(sessions, t);
                    map.putIfAbsent(t, reportConverter.toTimeSlotDto(t, actualMin, 0));
                }
                t = t.plusMinutes(SLOT_MINUTES);
            }
        }
    }

    private List<PeakWindowJson> parseWindows(String windowJson) {
        try {
            if (windowJson == null) return List.of();
            String trimmed = windowJson.trim();
            if (trimmed.isEmpty()) return List.of();

            if (trimmed.startsWith("[")) {
                return objectMapper.readValue(trimmed, new TypeReference<List<PeakWindowJson>>() {});
            }

            PeakTimeAiStoredJson wrapper = objectMapper.readValue(trimmed, PeakTimeAiStoredJson.class);
            return wrapper.top_peak_times() == null ? List.of() : wrapper.top_peak_times();

        } catch (Exception e) {
            throw new GeneralException(DailyReportErrorStatus.REPORT_JSON_PARSING_ERROR);
        }
    }

    private List<FocusSessionSlotCalculator.TimeRange> mergePeakWindows(List<PeakWindowJson> windows) {
        if (windows == null || windows.isEmpty()) return List.of();

        List<FocusSessionSlotCalculator.TimeRange> ranges = new ArrayList<>();
        for (PeakWindowJson w : windows) {
            if (w == null || w.hour() == null || w.duration() == null) continue;

            LocalTime start = toLocalTime(w.hour());
            int durationMinutes = (int) Math.round(w.duration() * 60.0);
            if (durationMinutes <= 0) continue;

            LocalTime end = start.plusMinutes(durationMinutes);
            ranges.add(new FocusSessionSlotCalculator.TimeRange(start, end));
        }
        if (ranges.isEmpty()) return List.of();

        ranges.sort(Comparator.comparing(FocusSessionSlotCalculator.TimeRange::start));

        List<FocusSessionSlotCalculator.TimeRange> merged = new ArrayList<>();
        FocusSessionSlotCalculator.TimeRange cur = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            FocusSessionSlotCalculator.TimeRange next = ranges.get(i);

            if (!next.start().isAfter(cur.end())) {
                LocalTime newEnd = next.end().isAfter(cur.end()) ? next.end() : cur.end();
                cur = new FocusSessionSlotCalculator.TimeRange(cur.start(), newEnd);
            } else {
                merged.add(cur);
                cur = next;
            }
        }
        merged.add(cur);

        return merged;
    }

    private List<FocusSessionSlotCalculator.TimeRange> extractSessionRanges(List<FocusSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return List.of();

        List<FocusSessionSlotCalculator.TimeRange> ranges = new ArrayList<>();

        for (FocusSession s : sessions) {
            LocalDateTime startedAt = s.getStartedAt();
            LocalDateTime endedAt = s.getEndedAt();
            if (startedAt == null || endedAt == null) continue;

            LocalTime start = startedAt.toLocalTime();
            LocalTime end = endedAt.toLocalTime();

            if (end.isBefore(start)) {
                end = LocalTime.MAX; //자정 넘어가는 경우
            }

            ranges.add(new FocusSessionSlotCalculator.TimeRange(
                    floorToSlot(start),
                    ceilToSlot(end)
            ));
        }

        if (ranges.isEmpty()) return List.of();

        ranges.sort(Comparator.comparing(FocusSessionSlotCalculator.TimeRange::start));

        List<FocusSessionSlotCalculator.TimeRange> merged = new ArrayList<>();
        FocusSessionSlotCalculator.TimeRange cur = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            FocusSessionSlotCalculator.TimeRange next = ranges.get(i);

            if (!next.start().isAfter(cur.end())) {
                LocalTime newEnd = next.end().isAfter(cur.end()) ? next.end() : cur.end();
                cur = new FocusSessionSlotCalculator.TimeRange(cur.start(), newEnd);
            } else {
                merged.add(cur);
                cur = next;
            }
        }
        merged.add(cur);

        return merged;
    }

    private LocalTime floorToSlot(LocalTime t) {
        int minute = t.getMinute();
        int floored = (minute < 30) ? 0 : 30;
        return LocalTime.of(t.getHour(), floored);
    }

    private LocalTime ceilToSlot(LocalTime t) {
        int h = t.getHour();
        int m = t.getMinute();
        if (m == 0) return LocalTime.of(h, 0);
        if (m <= 30) return LocalTime.of(h, 30);
        return LocalTime.of((h + 1) % 24, 0);
    }

    private LocalTime toLocalTime(Double hour) {
        int h = hour.intValue();
        int m = (Math.abs(hour - h) < 1e-9) ? 0 : 30;
        return LocalTime.of(h, m);
    }
}
