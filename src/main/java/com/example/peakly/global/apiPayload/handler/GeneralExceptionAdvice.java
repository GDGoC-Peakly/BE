package com.example.peakly.global.apiPayload.handler;

import com.example.peakly.global.apiPayload.ApiResponse;
import com.example.peakly.global.apiPayload.code.BaseErrorCode;
import com.example.peakly.global.apiPayload.code.status.AuthErrorStatus;
import com.example.peakly.global.apiPayload.code.status.ErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    /**
     * 1) 커스텀 예외 (GeneralException)
     * - 서비스/도메인에서 의도적으로 던진 예외
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> onGeneralException(
            GeneralException exception,
            HttpServletRequest request
    ) {
        return handleExceptionInternal(exception, exception.getCode(), null, request);
    }

    /**
     * 2) @Valid RequestBody 검증 실패
     * - DTO의 @NotBlank, @Email, @Size, @Pattern 등
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        FieldError fe = ex.getBindingResult().getFieldError();

        // 필드 정보가 없으면 공통 400
        if (fe == null) {
            ApiResponse<Object> body = ApiResponse.onFailure(ErrorStatus._BAD_REQUEST, null);
            return super.handleExceptionInternal(ex, body, headers, status, request);
        }

        BaseErrorCode mapped = mapFieldErrorToErrorCode(fe);

        ApiResponse<Object> body = ApiResponse.onFailure(mapped, null);
        return super.handleExceptionInternal(
                ex,
                body,
                headers,
                mapped.getReasonHttpStatus().getHttpStatus(),
                request
        );
    }

    /**
     * 3) Query / Path validation 실패 (@Validated)
     * - @RequestParam, @PathVariable 등
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> onConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        // 공통 BAD_REQUEST 처리
        return handleExceptionInternal(ex, ErrorStatus._BAD_REQUEST, HttpHeaders.EMPTY, request);
    }

    /**
     * 4) JSON 파싱 실패
     * - 잘못된 JSON, trailing comma, 타입 불일치 등
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ApiResponse<Object> body = ApiResponse.onFailure(ErrorStatus._BAD_REQUEST, null);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * 5) 정의되지 않은 모든 예외 (진짜 서버 에러)
     */
    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        log.error("Unhandled Exception 발생", e);
        return handleExceptionInternalFalse(
                e,
                ErrorStatus._INTERNAL_SERVER_ERROR,
                HttpHeaders.EMPTY,
                ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),
                request,
                "서버 내부 오류가 발생했습니다."
        );
    }

    /**
     * === 내부 공통 처리 ===
     */
    private ResponseEntity<Object> handleExceptionInternal(
            Exception e,
            BaseErrorCode code,
            HttpHeaders headers,
            HttpServletRequest request
    ) {
        ApiResponse<Object> body = ApiResponse.onFailure(code, null);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                code.getReasonHttpStatus().getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(
            Exception e,
            BaseErrorCode code,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request,
            String errorPoint
    ) {
        ApiResponse<Object> body = ApiResponse.onFailure(code, errorPoint);
        return super.handleExceptionInternal(e, body, headers, status, request);
    }

    /**
     * FieldError (필드명 + 제약조건)을 ErrorCode로 매핑
     *
     * - 매핑되지 않은 케이스는 무조건 ErrorStatus._BAD_REQUEST
     * - 잘못된 AuthErrorStatus로 떨어지는 것을 방지
     */
    // TODO: 도메인별 ValidationErrorMapper로 분리 (MVP 이후)
    private BaseErrorCode mapFieldErrorToErrorCode(FieldError fe) {
        String field = fe.getField();      // email, password, nickname, token ...
        String constraint = fe.getCode();  // NotBlank, Email, Size, Pattern ...

        // email
        if ("email".equals(field)) {
            if ("NotBlank".equals(constraint)) return AuthErrorStatus.EMAIL_REQUIRED;
            if ("Email".equals(constraint)) return AuthErrorStatus.INVALID_EMAIL_FORMAT;
            return AuthErrorStatus.INVALID_EMAIL_FORMAT;
        }

        // password
        if ("password".equals(field)) {
            if ("NotBlank".equals(constraint)) return AuthErrorStatus.PASSWORD_REQUIRED;
            return AuthErrorStatus.INVALID_PASSWORD_FORMAT;
        }

        // nickname
        if ("nickname".equals(field)) {
            if ("NotBlank".equals(constraint)) return AuthErrorStatus.NICKNAME_REQUIRED;
            return AuthErrorStatus.INVALID_NICKNAME_FORMAT;
        }

        // token (이메일 인증)
        if ("token".equals(field)) {
            if ("NotBlank".equals(constraint)) return AuthErrorStatus.EMAIL_TOKEN_REQUIRED;
            return AuthErrorStatus.EMAIL_TOKEN_REQUIRED;
        }

        // fallback: 공통 BAD_REQUEST
        return ErrorStatus._BAD_REQUEST;
    }
}
