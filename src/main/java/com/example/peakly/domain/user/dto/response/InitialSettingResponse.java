package com.example.peakly.domain.user.dto.response;

import com.example.peakly.domain.user.entity.Chronotype;
import com.example.peakly.domain.user.entity.Job;
import com.example.peakly.domain.user.entity.SubjectivePeaktime;

public record InitialSettingResponse(
        Long userId,
        Job job,
        Chronotype chronotype,
        SubjectivePeaktime subjectivePeaktime,
        int caffeineResponsiveness,
        int noiseSensitivity,
        String recordedAt
) {}
