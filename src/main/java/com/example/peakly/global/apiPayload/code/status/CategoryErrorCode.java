package com.example.peakly.global.apiPayload.code.status;

import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {
    MAJOR_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_001", "대분류를 찾을 수 없습니다."),
    CUSTOM_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_002", "커스텀 태그를 찾을 수 없습니다."),
    CUSTOM_TAG_FORBIDDEN(HttpStatus.FORBIDDEN, "CATEGORY_003", "해당 커스텀 태그에 대한 권한이 없습니다."),
    CUSTOM_TAG_NAME_DUPLICATE(HttpStatus.CONFLICT, "CATEGORY_004", "이미 존재하는 커스텀 태그 이름입니다."),
    INVALID_TAG_NAMES(HttpStatus.BAD_REQUEST, "CATEGORY_005", "태그 이름 목록이 올바르지 않습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "CATEGORY_006", "필수 입력값이 없습니다."),
    MAJOR_CATEGORY_ESSENTIAL(HttpStatus.BAD_REQUEST, "CATEGORY_007", "대분류 입력은 필수입니다."),
    NAME_NOT_EXIST(HttpStatus.BAD_REQUEST,"CATEGORY_008", "이름은 비어 있을 수 없습니다."),

    CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "CATEGORY404_003",
                    "카테고리를 찾을 수 없습니다."
    ),

    CATEGORY_MAJOR_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "CATEGORY400_005",
            "카테고리의 대분류가 일치하지 않습니다."
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
