package com.example.peakly.global.apiPayload.code;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ReasonDto {
    private HttpStatus httpStatus;
    private Boolean isSuccess;
    private String code;
    private String message;
}
