package com.example.peakly.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerifySendRequest(
        @NotBlank(message = "이메일 값이 필요합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {}
