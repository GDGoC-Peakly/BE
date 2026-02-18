package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FocusSessionErrorStatus implements BaseErrorCode {

    // =========================
    // 400 BAD_REQUEST
    // =========================

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "SESSION400_001",
            "잘못된 요청입니다."
    ),

    INVALID_TIME_VALUE(
            HttpStatus.BAD_REQUEST,
            "SESSION400_002",
            "시간 값이 올바르지 않습니다."
    ),

    INVALID_FOCUS_SCORE(
            HttpStatus.BAD_REQUEST,
            "SESSION400_003",
            "집중도 점수는 1~5 범위여야 합니다."
    ),

    // =========================
    // 401 UNAUTHORIZED
    // =========================

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "SESSION401_001",
            "인증이 필요합니다."
    ),

    // =========================
    // 403 FORBIDDEN
    // =========================

    SESSION_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "SESSION403_001",
            "세션에 대한 접근 권한이 없습니다."
    ),

    // =========================
    // 404 NOT_FOUND
    // =========================

    SESSION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SESSION404_001",
            "세션을 찾을 수 없습니다."
    ),

    // =========================
    // 409 CONFLICT
    // =========================

    SESSION_ALREADY_RUNNING(
            HttpStatus.CONFLICT,
            "SESSION409_001",
            "이미 진행 중인 세션이 있습니다."
    ),

    INVALID_SESSION_STATE(
            HttpStatus.CONFLICT,
            "SESSION409_002",
            "현재 세션 상태에서 수행할 수 없는 요청입니다."
    ),

    DATA_INCONSISTENCY(
            HttpStatus.CONFLICT,
            "SESSION409_003",
            "세션 상태 데이터가 일관되지 않습니다."
    ),

    SESSION_NOT_ENDED(
            HttpStatus.CONFLICT,
            "SESSION409_004",
            "종료되지 않은 세션은 평가할 수 없습니다."
    ),

    FEEDBACK_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "SESSION409_005",
            "이미 집중도 평가가 등록된 세션입니다."
    ),

    // =========================
    // 500 INTERNAL_SERVER_ERROR
    // =========================

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SESSION500_001",
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