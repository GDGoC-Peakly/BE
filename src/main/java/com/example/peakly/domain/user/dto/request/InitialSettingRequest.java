package com.example.peakly.domain.user.dto.request;

import com.example.peakly.domain.user.entity.Chronotype;
import com.example.peakly.domain.user.entity.Job;
import com.example.peakly.domain.user.entity.SubjectivePeaktime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InitialSettingRequest(
        @NotNull Job job,
        @NotNull Chronotype chronotype,
        @NotNull SubjectivePeaktime subjectivePeaktime,
        @Min(0) @Max(2) int caffeineResponsiveness,
        @Min(0) @Max(2) int noiseSensitivity
) {}
