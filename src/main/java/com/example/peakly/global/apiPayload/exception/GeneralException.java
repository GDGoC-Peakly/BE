package com.example.peakly.global.apiPayload.exception;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;
    private final Object payload;

    public GeneralException(BaseErrorCode code) {
        this(code, null);
    }

    public GeneralException(BaseErrorCode code, Object payload) {
        super(code.getReasonHttpStatus().getMessage()); // optional(로그/디버그에 도움)
        this.code = code;
        this.payload = payload;
    }

}
