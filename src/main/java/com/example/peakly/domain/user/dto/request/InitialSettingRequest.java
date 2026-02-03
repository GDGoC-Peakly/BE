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
        @NotNull (message = "카페인 반응도 값이 필요합니다.")
        @Min(0) @Max(2) Integer caffeineResponsiveness,
        @NotNull (message = "소음 민감도 값이 필요합니다.")
        @Min(0) @Max(2) Integer noiseSensitivity
) {}
