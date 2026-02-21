package com.example.peakly.domain.dailySleep.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SleepLogUpdateByTimeRequest(

        @Schema(
                description = "취침 시간 (24시간제, HH:mm). 한 자리 시/분은 0을 붙여 입력합니다. 예) 02:30, 23:05",
                example = "02:30",
                type = "string",
                pattern = "^([01]\\d|2[0-3]):[0-5]\\d$"
        )
        @NotNull(message = "취침 시간을 입력해주세요")
        @JsonFormat(pattern = "HH:mm")
        LocalTime bedTime,

        @Schema(
                description = "기상 시간 (24시간제, HH:mm). 한 자리 시/분은 0을 붙여 입력합니다. 예) 07:10, 06:00",
                example = "07:10",
                type = "string",
                pattern = "^([01]\\d|2[0-3]):[0-5]\\d$"
        )
        @NotNull(message = "기상 시간을 입력해주세요")
        @JsonFormat(pattern = "HH:mm")
        LocalTime wakeTime,

        @Schema(
                description = "수면 점수 (1~5)",
                example = "3",
                minimum = "1",
                maximum = "5"
        )
        @NotNull(message = "수면의 질을 체크해주세요")
        @Min(value = 1, message = "수면 점수는 1 이상이어야 합니다")
        @Max(value = 5, message = "수면 점수는 5 이하여야 합니다")
        Float sleepScore
) {}