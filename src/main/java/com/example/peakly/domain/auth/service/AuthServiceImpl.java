package com.example.peakly.domain.auth.service;

import com.example.peakly.domain.auth.command.AuthSessionIssueCommand;
import com.example.peakly.domain.auth.dto.request.*;
import com.example.peakly.domain.auth.dto.response.*;
import com.example.peakly.domain.auth.entity.AuthSession;
import com.example.peakly.domain.auth.entity.EmailVerificationToken;
import com.example.peakly.domain.auth.jwt.JwtTokenProvider;
import com.example.peakly.domain.auth.repository.AuthSessionRepository;
import com.example.peakly.domain.auth.repository.EmailVerificationTokenRepository;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionRepository authSessionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailVerifyMailSender emailVerifyMailSender;

    @Value("${jwt.refresh-token-exp-seconds}")
    private long refreshExpSeconds;

    @Value("${auth.email-verify.token-ttl-minutes:30}")
    private long emailVerifyTtlMinutes;

    @Value("${auth.email-verify.signup-window-minutes:30}")
    private long signupWindowMinutes;

    @Value("${auth.email-verify.rate-limit.window-seconds:60}")
    private long rateLimitWindowSeconds;

    @Value("${auth.email-verify.rate-limit.max-per-window:3}")
    private long rateLimitMaxPerWindow;

    @Override
    @Transactional(readOnly = true)
    public CheckEmailResponse checkEmail(CheckEmailRequest req) {
        boolean duplicated = userRepository.existsByEmail(req.email());
        return new CheckEmailResponse(req.email(), duplicated);
    }

    @Override
    @Transactional
    public EmailVerifySendResponse sendEmailVerify(EmailVerifySendRequest req) {
        String email = req.email();

        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_REGISTERED);
        }

        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        LocalDateTime after = now.minusSeconds(rateLimitWindowSeconds);
        long recent = emailVerificationTokenRepository.countByEmailAndCreatedAtAfter(email, after);
        if (recent >= rateLimitMaxPerWindow) {
            throw new GeneralException(AuthErrorStatus.EMAIL_VERIFY_RATE_LIMITED);
        }

        // 원문 토큰 발급 (메일 링크에 포함될 값)
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256Hex(rawToken);

        LocalDateTime expiresAt = now.plusMinutes(emailVerifyTtlMinutes);

        // TODO: requestIp/userAgent는 컨트롤러에서 받아서 넘기기
        EmailVerificationToken entity = EmailVerificationToken.issue(
                email,
                tokenHash,
                expiresAt,
                null,
                null
        );

        emailVerificationTokenRepository.save(entity);

        emailVerifyMailSender.sendVerifyMail(email, rawToken);

        entity.markSent(now);

        return new EmailVerifySendResponse(
                email,
                OffsetDateTime.now(DEFAULT_ZONE)
        );
    }

    @Override
    @Transactional
    public EmailVerifyResponse verifyEmail(EmailVerifyRequest req) {
        String rawToken = req.token();
        String tokenHash = sha256Hex(rawToken);

        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.EMAIL_VERIFICATION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);

        if (token.isUsed()) {
            throw new GeneralException(AuthErrorStatus.EMAIL_TOKEN_ALREADY_USED);
        }
        if (token.isExpired(now)) {
            throw new GeneralException(AuthErrorStatus.EMAIL_TOKEN_EXPIRED);
        }

        token.markUsed(now);
        // save 호출 없이도 영속 상태면 flush 시 반영됩니다.
        return new EmailVerifyResponse(true);
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest req) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        LocalDateTime verifiedAfter = now.minusMinutes(signupWindowMinutes);

        boolean verified = emailVerificationTokenRepository.existsByEmailAndUsedAtAfter(req.email(), verifiedAfter);
        if (!verified) {
            throw new GeneralException(AuthErrorStatus.EMAIL_VERIFICATION_REQUIRED);
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_REGISTERED);
        }

        String passwordHash = passwordEncoder.encode(req.password());

        User user = User.createEmailUser(req.email(), passwordHash, req.nickname());

        // (선택) 소셜 가입/탈퇴 유저 재가입 정책이 있으면 여기서 분기 필요
        // 현재는 단순 신규 생성 정책

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(AuthErrorStatus.EMAIL_ALREADY_REGISTERED);
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
                .orElseThrow(() -> new GeneralException(AuthErrorStatus.INVALID_EMAIL_OR_PASSWORD));

        if (user.getProvider() != AuthProvider.EMAIL) {
            throw new GeneralException(AuthErrorStatus.INVALID_EMAIL_OR_PASSWORD);
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new GeneralException(AuthErrorStatus.USER_DISABLED);
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new GeneralException(AuthErrorStatus.INVALID_EMAIL_OR_PASSWORD);
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
