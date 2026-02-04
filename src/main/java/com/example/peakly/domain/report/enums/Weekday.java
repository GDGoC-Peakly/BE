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

    /**
     * Determines whether this Weekday corresponds to the given DayOfWeek.
     *
     * @param dayOfWeek the DayOfWeek to compare against
     * @return {@code true} if this Weekday represents the same day as {@code dayOfWeek}, {@code false} otherwise
     */
    public boolean matches(DayOfWeek dayOfWeek) {
        return this.name().equalsIgnoreCase(dayOfWeek.name());
    }

    /**
     * Convert a java.time.DayOfWeek to the corresponding Weekday enum constant.
     *
     * @param dayOfWeek the DayOfWeek to convert
     * @return the Weekday constant matching the provided DayOfWeek
     */
    public static Weekday from(DayOfWeek dayOfWeek) {
        return Weekday.valueOf(dayOfWeek.name());
    }
}