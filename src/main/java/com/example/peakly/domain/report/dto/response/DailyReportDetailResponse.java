package com.example.peakly.domain.report.dto.response;

import com.example.peakly.domain.report.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;

public record DailyReportDetailResponse(
        LocalDate slotDate, //차트 기준 날짜
        String slotDayOfWeek,
        LocalDate statsDate,  //통계 기준 날짜
        Boolean statsReady,
        PeriodType periodType,
        Integer achievementRate,
        Integer accuracyRate,
        String insightMessage,

        PeakTimeStatsDto peakTimeStats
) {
    public record PeakTimeStatsDto(
            List<TimeSlotDto> timeSlots,
            Integer totalActualTime, // 분
            Integer totalTargetTime  // 분
    ) {}

    public record TimeSlotDto(
            String time,
            Integer actualMinutes,
            Integer targetMinutes
    ) {}
}
