package com.example.peakly.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin@example.com")
        @NotBlank(message = "이메일 값이 필요합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(example = "password1234")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
