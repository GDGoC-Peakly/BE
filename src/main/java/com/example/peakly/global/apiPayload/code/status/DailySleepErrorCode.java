package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DailySleepErrorCode implements BaseErrorCode {
    SLEEP_ALREADY_EXISTS(HttpStatus.CONFLICT, "SLEEP400_001", "오늘의 수면 기록이 이미 존재합니다. 수정을 이용해주세요."),
    SLEEP_NOT_FOUND(HttpStatus.NOT_FOUND, "SLEEP400_002", "해당 날짜의 수면 기록을 찾을 수 없습니다."),
    INVALID_OPERATION_TIME(HttpStatus.FORBIDDEN, "SLEEP400_003", "데이터 마감 시간(04:55~05:00)에는 등록 및 수정이 불가능합니다.")
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
