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
    INVALID_EMAIL_FORMAT(
            HttpStatus.BAD_REQUEST,
            "AUTH400_001",
            "이메일 형식이 올바르지 않습니다."
    ),
    EMAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH400_002",
            "이메일 값이 필요합니다."
    ),
    INVALID_JOB_VALUE(
            HttpStatus.BAD_REQUEST,
            "AUTH400_003",
            "직업 값이 올바르지 않습니다."
    ),
    PASSWORD_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH400_004",
            "비밀번호를 입력해주세요."
    ),
    NICKNAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH400_005",
            "닉네임을 입력해주세요."
    ),
    INVALID_PASSWORD_FORMAT(
            HttpStatus.BAD_REQUEST,
            "AUTH400_006",
            "비밀번호 형식이 올바르지 않습니다."),
    EMAIL_TOKEN_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "AUTH400_007",
            "토큰 값이 필요합니다."
    ),
    INVALID_NICKNAME_FORMAT(
            HttpStatus.BAD_REQUEST,
            "AUTH400_008",
            "닉네임 값이 올바르지 않습니다."),


    // 401
    INVALID_EMAIL_OR_PASSWORD(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_001",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),
    EMAIL_TOKEN_EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_003",
            "인증 토큰이 만료되었습니다."
    ),


    // 403
    USER_DISABLED(
            HttpStatus.FORBIDDEN,
            "AUTH403_001",
            "비활성화된 계정입니다."
    ),
    EMAIL_VERIFICATION_REQUIRED(
            HttpStatus.FORBIDDEN,
            "AUTH403_002",
            "이메일 인증이 필요합니다."
    ),


    // 404
    EMAIL_VERIFICATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_001",
            "인증 요청 내역이 없습니다."
    ),


    // 409
    EMAIL_ALREADY_REGISTERED(
            HttpStatus.CONFLICT,
            "AUTH409_001",
            "이미 가입된 이메일입니다."
    ),
    EMAIL_TOKEN_ALREADY_USED(
            HttpStatus.CONFLICT,
            "AUTH409_002",
            "이미 인증 완료된 요청입니다."
    ),

    // 429
    EMAIL_VERIFY_RATE_LIMITED(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_001",
            "요청이 너무 많습니다."
    ),

    ;


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
