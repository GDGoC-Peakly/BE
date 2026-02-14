package com.example.peakly.domain.auth.controller;

import com.example.peakly.domain.auth.dto.request.CheckEmailRequest;
import com.example.peakly.domain.auth.dto.request.LoginRequest;
import com.example.peakly.domain.auth.dto.request.SignupRequest;
import com.example.peakly.domain.auth.dto.request.EmailVerifySendRequest;
import com.example.peakly.domain.auth.dto.response.CheckEmailResponse;
import com.example.peakly.domain.auth.dto.response.SignupResponse;
import com.example.peakly.domain.auth.dto.response.LoginResponse;
import com.example.peakly.domain.auth.dto.response.EmailVerifySendResponse;
import com.example.peakly.domain.auth.dto.response.EmailVerifyResponse;
import com.example.peakly.domain.auth.service.AuthService;
import com.example.peakly.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-email")
    public ApiResponse<CheckEmailResponse> checkEmail(@Valid @RequestBody CheckEmailRequest req) {
        return ApiResponse.onSuccess(authService.checkEmail(req));
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ApiResponse.onSuccess(authService.signup(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.onSuccess(authService.login(req));
    }

    @PostMapping("/email-verify/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmailVerifySendResponse> sendEmailVerify(@Valid @RequestBody EmailVerifySendRequest req) {
        return ApiResponse.onSuccess(authService.sendEmailVerify(req));
    }

    @PostMapping("/email-verify")
    public ApiResponse<EmailVerifyResponse> verifyEmail(
            @RequestParam("token")
            @NotBlank(message = "토큰 값이 필요합니다.")
            String token
    ) {
        return ApiResponse.onSuccess(authService.verifyEmail(token));
    }
}
