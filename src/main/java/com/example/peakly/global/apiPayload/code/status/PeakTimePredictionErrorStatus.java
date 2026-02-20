package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PeakTimePredictionErrorStatus implements BaseErrorCode {

    // 404
    PEAKTIME_PREDICTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PeakTime404_001",
            "예측 데이터가 없습니다."),

    // 409
    AI_REQUEST_SOURCE_NOT_READY(
            HttpStatus.CONFLICT,
            "PeakTime409_001",
            "피크타임 예측에 필요한 데이터가 아직 준비되지 않았습니다."),

    // 500
    PREDICTION_UPSERT_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "PeakTime500_001",
            "피크타임 예측 데이터 저장 중 오류가 발생했습니다."
    ),

    // 502
    AI_BAD_RESPONSE(
            HttpStatus.BAD_GATEWAY,
            "PeakTime502_001",
            "AI 서버로부터 올바르지 않은 응답을 받았습니다."
    ),

    // 503
    AI_SERVER_ERROR(
            HttpStatus.SERVICE_UNAVAILABLE,
            "PeakTime503_001",
            "AI 서버와의 통신에 실패했습니다."
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
