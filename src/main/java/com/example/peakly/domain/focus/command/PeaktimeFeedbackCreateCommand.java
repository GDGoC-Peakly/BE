package com.example.peakly.domain.focus.command;

import java.util.List;

public record PeaktimeFeedbackCreateCommand(
        int focusScore,
        List<Long> disruptionReasonIds
) {
}
