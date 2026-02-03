package com.example.peakly.domain.auth.dto.request;

import com.example.peakly.domain.user.entity.Job;
import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank(message = "이메일 값이 필요합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "비밀번호는 영문자와 숫자를 모두 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {}
