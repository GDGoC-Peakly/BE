package com.example.peakly.domain.report.enums;

import java.time.DayOfWeek;

public enum Weekday {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public boolean matches(DayOfWeek dayOfWeek) {
        return this.name().equalsIgnoreCase(dayOfWeek.name());
    }

    public static Weekday from(DayOfWeek dayOfWeek) {
        return Weekday.valueOf(dayOfWeek.name());
    }
}
