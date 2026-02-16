package com.example.peakly.domain.report.service.daily;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyReportDetailServiceImpl implements DailyReportDetailService {

    // AI 코드 session_hours = 1.5 기준, 피크 윈도우 1개당 90분 고정
    private static final int PEAK_WINDOW_MINUTES = 90; // TODO : AI 반환값 확정되면 수정
    private static final int SLOT_MINUTES = 30;

    private final DailyReportDetailRepository detailRepository;
    private final PeakTimePredictionRepository peakTimeRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final FocusSessionSlotCalculator slotCalculator;
    private final ReportConverter reportConverter;
    private final ObjectMapper objectMapper;

    @Override
    public DailyReportDetailResponse getDailyReport(Long userId, LocalDate date) {
        DailyReport report = detailRepository.findByUserIdAndReportDate(userId, date)
                .orElseThrow(() -> new GeneralException(DailyReportErrorStatus.DAILY_REPORT_NOT_FOUND));

        // 피크 타임 예측 조회
        PeakTimePrediction prediction = peakTimeRepository.findTopByUserIdOrderByBaseDateDesc(userId)
                .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PREDICTION_NOT_FOUND));

        List<FocusSession> sessions = focusSessionRepository
                .findByUser_IdAndBaseDateAndSessionStatus(userId, date, SessionStatus.ENDED);

        // 30분 단위 슬롯 생성
        List<DailyReportDetailResponse.TimeSlotDto> timeSlots = create30MinSlots(prediction.getWindowJson(), sessions);

        return reportConverter.toDetailResponse(report, timeSlots);
    }

    private List<DailyReportDetailResponse.TimeSlotDto> create30MinSlots(
            String windowJson, List<FocusSession> sessions) {
        try {
            List<PeakWindowJson> top3Windows = objectMapper.readValue(
                    windowJson, new TypeReference<List<PeakWindowJson>>() {});

            List<DailyReportDetailResponse.TimeSlotDto> slots = new ArrayList<>();

            for (PeakWindowJson window : top3Windows) {
                int elapsed = 0;
                while (elapsed < PEAK_WINDOW_MINUTES) {
                    LocalTime slotStart = LocalTime.of(window.hour(), 0).plusMinutes(elapsed);
                    int actualMin = slotCalculator.calcActualMinInSlot(sessions, slotStart);
                    // 슬롯 절대 시간 계산
                    slots.add(reportConverter.toTimeSlotDto(slotStart, actualMin, SLOT_MINUTES));
                    elapsed += SLOT_MINUTES;
                }
            }
            return slots;

        } catch (JsonProcessingException e) {
            throw new GeneralException(DailyReportErrorStatus.REPORT_JSON_PARSING_ERROR);
        }
    }
}