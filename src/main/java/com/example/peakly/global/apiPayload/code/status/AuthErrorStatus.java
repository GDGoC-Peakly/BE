package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    // 400
    AUTH_400_001(HttpStatus.BAD_REQUEST, "AUTH400_001", "이메일 형식이 올바르지 않습니다."),
    AUTH_400_002(HttpStatus.BAD_REQUEST, "AUTH400_002", "이메일 값이 필요합니다."),
    AUTH_400_003(HttpStatus.BAD_REQUEST, "AUTH400_003", "직업 값이 올바르지 않습니다."),
    AUTH_400_004(HttpStatus.BAD_REQUEST, "AUTH400_004", "비밀번호를 입력해주세요."),
    AUTH_400_005(HttpStatus.BAD_REQUEST, "AUTH400_005", "닉네임을 입력해주세요."),

    // 401
    AUTH_401_001(HttpStatus.UNAUTHORIZED, "AUTH401_001", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 403
    AUTH_403_001(HttpStatus.FORBIDDEN, "AUTH403_001", "비활성화된 계정입니다."),

    // 409
    AUTH_409_001(HttpStatus.CONFLICT, "AUTH409_001", "이미 가입된 이메일입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .message(message)
                .code(code)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
