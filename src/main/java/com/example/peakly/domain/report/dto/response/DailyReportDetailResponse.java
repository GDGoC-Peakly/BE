package com.example.peakly.domain.report.dto.response;

import com.example.peakly.domain.report.enums.PeriodType;
import java.time.LocalDate;
import java.util.List;

public record DailyReportDetailResponse(
        LocalDate date,
        String dayOfWeek,
        PeriodType periodType,
        Integer achievementRate,
        Integer accuracyRate,
        String insightMessage,
        PeakTimeStatsDto peakTimeStats

        ) {
    public record PeakTimeStatsDto(
            List<TimeSlotDto> timeSlots,
            Integer totalActualTime,
            Integer totalTargetTime
    ) {}

    public record TimeSlotDto(
            String time,
            Integer actualMinutes,
            Integer targetMinutes
    ) {}

}
