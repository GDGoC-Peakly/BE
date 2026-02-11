package com.example.peakly.domain.auth.service;

import com.example.peakly.domain.auth.dto.request.CheckEmailRequest;
import com.example.peakly.domain.auth.dto.request.LoginRequest;
import com.example.peakly.domain.auth.dto.request.SignupRequest;
import com.example.peakly.domain.auth.dto.request.EmailVerifySendRequest;
import com.example.peakly.domain.auth.dto.request.EmailVerifyRequest;
import com.example.peakly.domain.auth.dto.response.CheckEmailResponse;
import com.example.peakly.domain.auth.dto.response.SignupResponse;
import com.example.peakly.domain.auth.dto.response.LoginResponse;
import com.example.peakly.domain.auth.dto.response.EmailVerifySendResponse;
import com.example.peakly.domain.auth.dto.response.EmailVerifyResponse;

public interface AuthService {
    CheckEmailResponse checkEmail(CheckEmailRequest req);
    SignupResponse signup(SignupRequest req);
    LoginResponse login(LoginRequest req);

    EmailVerifySendResponse sendEmailVerify(EmailVerifySendRequest req);
    EmailVerifyResponse verifyEmail(EmailVerifyRequest req);
}
