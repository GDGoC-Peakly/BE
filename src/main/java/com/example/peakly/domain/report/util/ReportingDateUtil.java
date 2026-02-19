package com.example.peakly.domain.report.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ReportingDateUtil {

    public static final LocalTime CUTOFF_TIME = LocalTime.of(5, 0);

    private ReportingDateUtil() {}

    // endedAt 기준 -> 05:00 이전이면 전날, 이후면 당일
    public static LocalDate reportingDateOf(LocalDateTime endedAt) {
        if (endedAt == null) return null;
        LocalDate date = endedAt.toLocalDate();
        if (endedAt.toLocalTime().isBefore(CUTOFF_TIME)) {
            return date.minusDays(1);
        }
        return date;
    }
}
