package com.example.peakly.domain.report.converter;

import com.example.peakly.domain.report.dto.response.DailyReportDetailResponse;
import com.example.peakly.domain.report.entity.DailyReport;
import com.example.peakly.domain.report.enums.PeriodType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportConverter {

    public DailyReportDetailResponse toDetailResponse(
            LocalDate slotDate,
            String slotDayOfWeek,
            LocalDate statsDate,
            DailyReport statsReport,

            List<DailyReportDetailResponse.TimeSlotDto> peakSlots,
            List<DailyReportDetailResponse.TimeSlotDto> nonPeakSlots,

            int peakActualMin,
            int peakTargetMin,

            int nonPeakActualMin
    ) {
        boolean ready = (statsReport != null);

        Integer achievement = ready ? (int) Math.round(statsReport.getAchievementRate()) : null;
        Integer accuracy = ready ? (int) Math.round(statsReport.getAccuracyRate()) : null;
        String insightMsg = ready && statsReport.getInsight() != null
                ? statsReport.getInsight().getMessage()
                : null;

        return new DailyReportDetailResponse(
                slotDate,
                slotDayOfWeek,
                statsDate,
                ready,
                PeriodType.DAILY,
                achievement,
                accuracy,
                insightMsg,
                new DailyReportDetailResponse.PeakTimeStatsDto(
                        peakSlots,
                        peakActualMin,
                        peakTargetMin
                ),
                new DailyReportDetailResponse.NonPeakTimeStatsDto(
                        nonPeakSlots,
                        nonPeakActualMin
                )
        );
    }

    public DailyReportDetailResponse.TimeSlotDto toTimeSlotDto(
            LocalTime slotStart, Integer actualMinutes, Integer targetMinutes
    ) {
        return new DailyReportDetailResponse.TimeSlotDto(
                slotStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                actualMinutes,
                targetMinutes
        );
    }
}