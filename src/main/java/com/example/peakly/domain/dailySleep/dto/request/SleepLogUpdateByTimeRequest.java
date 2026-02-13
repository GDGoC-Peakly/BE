package com.example.peakly.domain.dailySleep.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SleepLogUpdateByTimeRequest(
        @NotNull(message = "취침 시간을 입력해주세요")
        @JsonFormat(pattern = "HH:mm")
        LocalTime bedTime,
        @NotNull(message = "기상 시간을 입력해주세요")
        @JsonFormat(pattern = "HH:mm")
        LocalTime wakeTime,
        @NotNull(message = "수면의 질을 체크해주세요")
        Integer sleepScore
) {
}
