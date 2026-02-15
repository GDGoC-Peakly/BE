package com.example.peakly.domain.focusSession.command;

import java.util.List;

public record PeaktimeFeedbackCreateCommand(
        int focusScore,
        List<Long> disruptionReasonIds
) {
}
