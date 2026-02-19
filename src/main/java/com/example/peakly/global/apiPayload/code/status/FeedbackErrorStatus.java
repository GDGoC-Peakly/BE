package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FeedbackErrorStatus implements BaseErrorCode {

    // =========================
    // 400 BAD_REQUEST
    // =========================
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "FEEDBACK400_001",
            "잘못된 요청입니다."
    ),
    DISRUPTION_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "FEEDBACK400_002",
            "방해 요인을 1개 이상 선택해야 합니다."
    ),
    INVALID_DISRUPTION_REASON_ID(
            HttpStatus.BAD_REQUEST,
            "FEEDBACK400_003",
            "방해 요인 ID가 올바르지 않습니다."
    ),
    INACTIVE_DISRUPTION_REASON(
            HttpStatus.BAD_REQUEST,
            "FEEDBACK400_004",
            "비활성화된 방해 요인입니다."
    ),

    // =========================
    // 404 NOT_FOUND
    // =========================
    FEEDBACK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FEEDBACK404_001",
            "집중도 평가가 존재하지 않습니다."
    ),

    // =========================
    // 409 CONFLICT
    // =========================
    DISRUPTION_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "FEEDBACK409_001",
            "이미 방해 요인이 등록된 세션입니다."
    ),
    DISRUPTION_NOT_ALLOWED_FOR_HIGH_SCORE(
            HttpStatus.CONFLICT,
            "FEEDBACK409_003",
            "집중도 점수가 2점 이하인 경우에만 방해 요인을 등록할 수 있습니다."
    ),

    // =========================
    // 500 INTERNAL_SERVER_ERROR
    // =========================
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FEEDBACK500_001",
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
