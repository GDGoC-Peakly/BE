package com.example.peakly.domain.report.dto.response;

import com.example.peakly.domain.report.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;

public record DailyReportDetailResponse(
        LocalDate slotDate, // 차트 기준 날짜
        String slotDayOfWeek,
        LocalDate statsDate,  // 통계 기준 날짜(전날)
        Boolean statsReady,
        PeriodType periodType,

        Integer achievementRate,
        Integer accuracyRate,
        String insightMessage,

        PeakTimeStatsDto peakTimeStats,     // 피크타임 구간
        NonPeakTimeStatsDto nonPeakTimeStats // 비피크 구간
) {

    public record PeakTimeStatsDto(
            List<TimeSlotDto> timeSlots,
            Integer totalActualTime,
            Integer totalTargetTime
    ) {}

    public record NonPeakTimeStatsDto(
            List<TimeSlotDto> timeSlots,
            Integer totalActualTime
    ) {}

    public record TimeSlotDto(
            String time,
            Integer actualMinutes,
            Integer targetMinutes // 피크: 30, 비피크: 0
    ) {}
}
