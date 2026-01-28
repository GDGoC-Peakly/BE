package com.example.peakly.domain.user.command;

import com.example.peakly.domain.user.entity.Chronotype;

public record InitialDataCreateCommand(
        Chronotype chronotype,
        int caffeineResponsiveness,
        int noiseSensitivity
) {
}
