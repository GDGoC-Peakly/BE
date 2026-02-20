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
                .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PEAKTIME_PREDICTION_NOT_FOUND));

        List<FocusSession> slotSessions = focusSessionRepository
                .findByUser_IdAndBaseDateAndSessionStatus(userId, slotDate, SessionStatus.ENDED);

        List<FocusSession> counted = slotSessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        // TODO: 엔티티에 맞춰 수정
        List<PeakWindowJson> windows = null;
//        List<PeakWindowJson> windows = parseWindows(prediction.getWindowJson());
        List<FocusSessionSlotCalculator.DateTimeRange> peakRanges = toPeakRanges(slotDate, windows);

        List<FocusSessionSlotCalculator.DateTimeRange> sessionRanges = extractSessionRanges(counted);

        // 피크타임 슬롯 생성
        Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> peakMap = new LinkedHashMap<>();
        putSlots(peakMap, peakRanges, counted, true);

        // 비피크타임 슬롯 생성
        Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> nonPeakMap = new LinkedHashMap<>();
        putSlotsExcludeKeys(nonPeakMap, sessionRanges, counted, peakMap.keySet());

        List<DailyReportDetailResponse.TimeSlotDto> peakSlots = new ArrayList<>(peakMap.values());
        List<DailyReportDetailResponse.TimeSlotDto> nonPeakSlots = new ArrayList<>(nonPeakMap.values());

        int peakActualMin = peakSlots.stream().mapToInt(ts -> nvl(ts.actualMinutes())).sum();
        int peakTargetMin = peakSlots.stream().mapToInt(ts -> nvl(ts.targetMinutes())).sum();
        int nonPeakActualMin = nonPeakSlots.stream().mapToInt(ts -> nvl(ts.actualMinutes())).sum();

        DailyReport statsReport = dailyReportRepository
                .findByUserIdAndReportDate(userId, statsDate)
                .orElse(null);

        return reportConverter.toDetailResponse(
                slotDate,
                slotDate.getDayOfWeek().name(),
                statsDate,
                statsReport,
                peakSlots,
                nonPeakSlots,
                peakActualMin,
                peakTargetMin,
                nonPeakActualMin
        );
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    // ranges(날짜 포함) 기준으로 30분 슬롯 생성
    private void putSlots(
            Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> map,
            List<FocusSessionSlotCalculator.DateTimeRange> ranges,
            List<FocusSession> sessions,
            boolean isPeak
    ) {
        if (ranges == null || ranges.isEmpty()) return;

        int targetMin = isPeak ? SLOT_MINUTES : 0;

        for (FocusSessionSlotCalculator.DateTimeRange r : ranges) {
            if (r == null) continue;

            LocalDateTime t = r.start();
            while (t.isBefore(r.end())) {
                LocalTime key = t.toLocalTime();

                int actualMin = slotCalculator.calcActualMinInSlot(sessions, t);
                map.putIfAbsent(key, reportConverter.toTimeSlotDto(key, actualMin, targetMin));

                t = t.plusMinutes(SLOT_MINUTES);
            }
        }
    }

    private void putSlotsExcludeKeys(
            Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> map,
            List<FocusSessionSlotCalculator.DateTimeRange> ranges,
            List<FocusSession> sessions,
            Set<LocalTime> exclude
    ) {
        if (ranges == null || ranges.isEmpty()) return;

        for (FocusSessionSlotCalculator.DateTimeRange r : ranges) {
            if (r == null) continue;

            LocalDateTime t = r.start();
            while (t.isBefore(r.end())) {
                LocalTime key = t.toLocalTime();

                if (!exclude.contains(key)) {
                    int actualMin = slotCalculator.calcActualMinInSlot(sessions, t);
                    map.putIfAbsent(key, reportConverter.toTimeSlotDto(key, actualMin, 0));
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

    private List<FocusSessionSlotCalculator.DateTimeRange> toPeakRanges(LocalDate slotDate, List<PeakWindowJson> windows) {
        if (windows == null || windows.isEmpty()) return List.of();

        List<FocusSessionSlotCalculator.DateTimeRange> ranges = new ArrayList<>();
        for (PeakWindowJson w : windows) {
            if (w == null || w.hour() == null || w.duration() == null) continue;

            LocalTime startT = toLocalTime(w.hour());
            int durationMinutes = (int) Math.round(w.duration() * 60.0);
            if (durationMinutes <= 0) continue;

            LocalDateTime start = slotDate.atTime(startT);
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


    private List<FocusSessionSlotCalculator.DateTimeRange> extractSessionRanges(List<FocusSession> sessions) {
        if (sessions == null || sessions.isEmpty()) return List.of();

        List<FocusSessionSlotCalculator.DateTimeRange> ranges = new ArrayList<>();

        for (FocusSession s : sessions) {
            LocalDateTime startedAt = s.getStartedAt();
            LocalDateTime endedAt = s.getEndedAt();
            if (startedAt == null || endedAt == null) continue;

            LocalDateTime start = floorToSlot(startedAt);
            LocalDateTime end = ceilToSlot(endedAt);

            if (end.isBefore(start)) end = start;

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

    private LocalDateTime floorToSlot(LocalDateTime dt) {
        int m = dt.getMinute();
        int floored = (m < 30) ? 0 : 30;
        return dt.withMinute(floored).withSecond(0).withNano(0);
    }

    private LocalDateTime ceilToSlot(LocalDateTime dt) {
        // 이미 정확히 경계면 그대로
        if (dt.getSecond() == 0 && dt.getNano() == 0) {
            int m = dt.getMinute();
            if (m == 0 || m == 30) return dt;
        }

        int m = dt.getMinute();
        if (m == 0) return dt.withSecond(0).withNano(0);
        if (m <= 30) return dt.withMinute(30).withSecond(0).withNano(0);
        return dt.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalTime toLocalTime(Double hour) {
        int h = hour.intValue();
        int m = (Math.abs(hour - h) < 1e-9) ? 0 : 30;
        return LocalTime.of(h, m);
    }
}