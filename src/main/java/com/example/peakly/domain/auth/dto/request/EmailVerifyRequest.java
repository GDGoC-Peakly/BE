package com.example.peakly.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerifyRequest(
        @NotBlank(message = "토큰 값이 필요합니다.")
        String token
) {}
