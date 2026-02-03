package com.example.peakly.domain.auth.service;

import com.example.peakly.domain.auth.dto.request.CheckEmailRequest;
import com.example.peakly.domain.auth.dto.request.LoginRequest;
import com.example.peakly.domain.auth.dto.request.SignupRequest;
import com.example.peakly.domain.auth.dto.response.CheckEmailResponse;
import com.example.peakly.domain.auth.dto.response.LoginResponse;
import com.example.peakly.domain.auth.dto.response.SignupResponse;
import com.example.peakly.domain.auth.jwt.JwtTokenProvider;
import com.example.peakly.domain.user.entity.AuthProvider;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.entity.UserStatus;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.AuthErrorStatus;
import com.example.peakly.global.apiPayload.code.status.ErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public CheckEmailResponse checkEmail(CheckEmailRequest req) {
        boolean duplicated = userRepository.existsByEmail(req.email());
        return new CheckEmailResponse(req.email(), duplicated);
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new GeneralException(AuthErrorStatus.AUTH_409_001);
        }

        String passwordHash = passwordEncoder.encode(req.password());

        User user = User.createEmailUser(req.email(), passwordHash, req.nickname());

        // (선택) 소셜 가입/탈퇴 유저 재가입 정책이 있으면 여기서 분기 필요
        // 현재는 단순 신규 생성 정책

        User saved = userRepository.save(user);

        return new SignupResponse(saved.getId(), saved.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.AUTH_401_001));

        if (user.getProvider() != AuthProvider.EMAIL) {
            throw new GeneralException(AuthErrorStatus.AUTH_401_001);
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new GeneralException(AuthErrorStatus.AUTH_403_001);
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new GeneralException(AuthErrorStatus.AUTH_401_001);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                new LoginResponse.UserSummary(
                        user.getId(),
                        user.getEmail(),
                        user.getJob()
                )
        );
    }
}
