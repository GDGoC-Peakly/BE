package com.example.peakly.domain.peakTimePrediction.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PeakTimePredictRequest(
        String user_id,
        UserProfile user_profile,
        List<RecentRecord> recent_records
) {
    public record UserProfile(
            @JsonProperty("chronotype") String chronotype,
            @JsonProperty("caffeine_sensitivity_prior") String caffeineSensitivityPrior,
            @JsonProperty("noise_senserance_prior") String noiseSenserancePrior,
            @JsonProperty("optimal_hours") Double optimalHours
    ) {}

    public record RecentRecord(
            String date,          // YYYY-MM-DD
            Double sleep_feeling,
            Integer fatigue_level
    ) {}
}