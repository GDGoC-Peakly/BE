package com.example.peakly.domain.user.command;

import com.example.peakly.domain.user.entity.Chronotype;
import com.example.peakly.domain.user.entity.SubjectivePeaktime;

public record InitialDataCreateCommand(
        Chronotype chronotype,
        SubjectivePeaktime subjectivePeaktime,
        int caffeineResponsiveness,
        int noiseSensitivity
) {
}
