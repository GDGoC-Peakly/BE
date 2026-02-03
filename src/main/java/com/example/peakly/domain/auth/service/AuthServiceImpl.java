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

        // 이메일 회원가입: provider=EMAIL, userStatus=ACTIVE
        User user = User.createEmailUser(req.email(), passwordHash, req.nickname());

        // (선택) 소셜 가입/탈퇴 유저 재가입 정책이 있으면 여기서 분기 필요
        // 현재는 단순 신규 생성 정책

        User saved = userRepository.save(user);

        // BaseEntity(@CreatedDate) 기반 createdAt 사용 (확정)
        return new SignupResponse(saved.getId(), saved.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new GeneralException(ErrorStatus._UNAUTHORIZED)); // AUTH_401_001 권장

        // 이메일 로그인 API인데 provider가 EMAIL이 아닌 계정이 들어오면 정책이 필요합니다.
        // 지금 요구사항에 없으니 "로그인 실패"로 통일(실무에서도 흔함).
        if (user.getProvider() != AuthProvider.EMAIL) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new GeneralException(ErrorStatus._FORBIDDEN); // AUTH_403_001 권장
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
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
