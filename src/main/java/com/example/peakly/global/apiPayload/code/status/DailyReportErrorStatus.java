package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DailyReportErrorStatus implements BaseErrorCode {

    DAILY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT404_001", "해당 날짜의 리포트가 존재하지 않습니다."),
    REPORT_JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT500_001", "리포트 데이터 형식이 올바르지 않아 읽어올 수 없습니다.");
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
