package com.example.peakly.domain.report.converter;

import com.example.peakly.domain.report.dto.response.DailyReportDetailResponse;
import com.example.peakly.domain.report.entity.DailyReport;
import com.example.peakly.domain.report.enums.PeriodType;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportConverter {

    public DailyReportDetailResponse toDetailResponse(
            DailyReport report,
            List<DailyReportDetailResponse.TimeSlotDto> timeSlots
    ){
        return new DailyReportDetailResponse(
                report.getReportDate(),
                report.getWeekday().name(),
                PeriodType.DAILY,
                (int) Math.round(report.getAchievementRate()), //소수점 반올림
                (int) Math.round(report.getAccuracyRate()),
                report.getInsight() != null
                        ? report.getInsight().getMessage()
                        : null,
                new DailyReportDetailResponse.PeakTimeStatsDto(
                        timeSlots,
                        report.getTotalFocusSec() / 60, // 초 -> 분 변환
                        report.getTotalTargetSec() / 60
                )
        );
    }

    // 시간대별 데이터 Dto 생성
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
