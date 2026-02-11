package com.example.peakly.domain.auth.dto.request;

import com.example.peakly.domain.user.entity.Job;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record SignupRequest(
        @Schema(example = "user@example.com")
        @NotBlank(message = "이메일 값이 필요합니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(example = "password1234")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8~72자여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "비밀번호는 영문자와 숫자를 모두 포함해야 합니다."
        )
        String password,

        @Schema(example = "픽꾸잉")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {}
