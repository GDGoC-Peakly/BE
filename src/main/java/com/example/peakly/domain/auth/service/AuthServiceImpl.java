package com.example.peakly.domain.auth.service;

import com.example.peakly.domain.auth.command.AuthSessionIssueCommand;
import com.example.peakly.domain.auth.dto.request.CheckEmailRequest;
import com.example.peakly.domain.auth.dto.request.LoginRequest;
import com.example.peakly.domain.auth.dto.request.SignupRequest;
import com.example.peakly.domain.auth.dto.response.CheckEmailResponse;
import com.example.peakly.domain.auth.dto.response.LoginResponse;
import com.example.peakly.domain.auth.dto.response.SignupResponse;
import com.example.peakly.domain.auth.entity.AuthSession;
import com.example.peakly.domain.auth.jwt.JwtTokenProvider;
import com.example.peakly.domain.auth.repository.AuthSessionRepository;
import com.example.peakly.domain.user.entity.AuthProvider;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.entity.UserStatus;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.AuthErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionRepository authSessionRepository;

    @Value("${jwt.refresh-token-exp-seconds}")
    private long refreshExpSeconds;

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

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(AuthErrorStatus.AUTH_409_001);
        }

        return new SignupResponse(
                saved.getId(),
                saved.getCreatedAt()
                        .atZone(DEFAULT_ZONE)
                        .toOffsetDateTime());
    }

    @Override
    @Transactional
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

        String deviceId = "local-test"; // TODO: 나중에 헤더(X-Device-Id)로 받기
        LocalDateTime issuedAt = LocalDateTime.now(DEFAULT_ZONE);
        LocalDateTime expiresAt = issuedAt.plusSeconds(refreshExpSeconds);

        String refreshTokenHash = sha256Hex(refreshToken);

        AuthSession session = AuthSession.issue(
                user,
                new AuthSessionIssueCommand(deviceId, refreshTokenHash, issuedAt, expiresAt)
        );
        authSessionRepository.save(session);

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

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest); // 64 chars
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }
    }
}
