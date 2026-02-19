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

        List<FocusSession> countedSlotSessions = slotSessions.stream()
                .filter(FocusSession::isCountedInStats)
                .toList();

        List<PeakWindowJson> windows = parseWindows(prediction.getWindowJson());
        List<FocusSessionSlotCalculator.TimeRange> mergedRanges = mergePeakWindows(windows);
        List<DailyReportDetailResponse.TimeSlotDto> timeSlots =
                createSlotsFromRanges(mergedRanges, countedSlotSessions);

        int slotTotalFocusMin = timeSlots.stream()
                .mapToInt(ts -> ts.actualMinutes() == null ? 0 : ts.actualMinutes())
                .sum();

        int slotTotalTargetMin = timeSlots.stream()
                .mapToInt(ts -> ts.targetMinutes() == null ? 0 : ts.targetMinutes())
                .sum();

        DailyReport statsReport = dailyReportRepository
                .findByUserIdAndReportDate(userId, statsDate)
                .orElse(null);

        return reportConverter.toDetailResponse(
                slotDate,
                slotDate.getDayOfWeek().name(),
                statsDate,
                statsReport,
                timeSlots,
                slotTotalFocusMin,
                slotTotalTargetMin
        );
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

            if (!next.start().isAfter(cur.end())) { // 겹치거나 이어짐
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

    private List<DailyReportDetailResponse.TimeSlotDto> createSlotsFromRanges(
            List<FocusSessionSlotCalculator.TimeRange> ranges,
            List<FocusSession> sessions
    ) {
        if (ranges == null || ranges.isEmpty()) return List.of();

        // time order 유지 + 중복 제거
        Map<LocalTime, DailyReportDetailResponse.TimeSlotDto> slotMap = new LinkedHashMap<>();

        for (FocusSessionSlotCalculator.TimeRange r : ranges) {
            LocalTime t = r.start();

            while (t.isBefore(r.end())) {
                int actualMin = slotCalculator.calcActualMinInSlot(sessions, t);
                slotMap.putIfAbsent(t, reportConverter.toTimeSlotDto(t, actualMin, SLOT_MINUTES));
                t = t.plusMinutes(SLOT_MINUTES);
            }
        }

        return new ArrayList<>(slotMap.values());
    }

    private LocalTime toLocalTime(Double hour) {
        int h = hour.intValue();
        int m = (Math.abs(hour - h) < 1e-9) ? 0 : 30;
        return LocalTime.of(h, m);
    }
}