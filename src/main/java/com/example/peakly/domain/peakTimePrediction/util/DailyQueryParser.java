package com.example.peakly.domain.peakTimePrediction.util;

import com.example.peakly.global.apiPayload.code.status.DailyErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public final class DailyQueryParser {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DailyQueryParser() {}

    public static LocalDate parseBaseDateOrToday(String baseDateStr) {
        if (baseDateStr == null || baseDateStr.isBlank()) {
            return LocalDate.now(KST);
        }
        try {
            return LocalDate.parse(baseDateStr); // YYYY-MM-DD
        } catch (DateTimeParseException e) {
            throw new GeneralException(DailyErrorStatus.INVALID_BASE_DATE);
        }
    }
}