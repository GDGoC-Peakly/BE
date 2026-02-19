package com.example.peakly.domain.report.dto.response;

import com.example.peakly.domain.report.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;

public record DailyReportDetailResponse(
        LocalDate slotDate,
        String slotDayOfWeek,
        LocalDate statsDate,
        Boolean statsReady,
        PeriodType periodType,
        Integer achievementRate,
        Integer accuracyRate,
        String insightMessage,
        List<TimeSlotDto> peakTimeSlots,
        List<TimeSlotDto> otherTimeSlots,
        Integer peakTotalActualTime,
        Integer peakTotalTargetTime,
        Integer otherTotalActualTime,
        Integer otherTotalTargetTime
) {
    public record TimeSlotDto(
            String time,
            Integer actualMinutes,
            Integer targetMinutes
    ) {}
}