package com.example.peakly.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequest(
        @Schema(example = "e78814284e6c47b3bbe96bb840194abe")
        @NotBlank(message = "토큰 값이 필요합니다.")
        String token
) {}
