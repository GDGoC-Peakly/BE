package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {

    // 400
    INVALID_USER_REQUEST(
            HttpStatus.BAD_REQUEST,
            "USER400_001",
            "잘못된 요청입니다."
    ),

    INVALID_ENUM_VALUE(
            HttpStatus.BAD_REQUEST,
            "USER400_002",
            "ENUM 값이 올바르지 않습니다."
    ),

    VALUE_OUT_OF_RANGE(
            HttpStatus.BAD_REQUEST,
            "USER400_003",
            "값 범위가 올바르지 않습니다."
    ),


    // 401
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_001",
            "인증이 필요합니다."
    ),


    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,
            "USER404_001",
            "존재하지 않는 사용자입니다."),


    // 409
    INITIAL_DATA_ALREADY_REGISTERED(HttpStatus.CONFLICT,
            "USER409_001",
            "초기 데이터가 이미 등록되어 있습니다."),


    // 500
    USER_INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "USER500_001",
                    "서버 에러가 발생했습니다."
    );


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
