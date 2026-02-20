package com.example.peakly.domain.peakTimePrediction.dto.ai;

import java.util.List;

public record PeakTimePredictResponse(
        String user_id,
        List<TopPeakTime> top_peak_times
) {
    public record TopPeakTime(
            Double hour,      // 0~24
            Double duration,  // hours
            Double score      // raw score
    ) {}
}