package com.example.peakly.global.apiPayload.code;

public interface BaseErrorCode {
    ErrorReasonDto getReason();
    ErrorReasonDto getReasonHttpStatus();
}
