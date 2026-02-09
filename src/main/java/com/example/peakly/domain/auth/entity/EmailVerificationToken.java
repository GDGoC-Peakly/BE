package com.example.peakly.domain.auth.entity;

import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "email_verification_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_evt_token_hash", columnNames = "token_hash")
        },
        indexes = {
                @Index(name = "idx_evt_email", columnList = "email"),
                @Index(name = "idx_evt_expires_at", columnList = "expires_at")
        }
)
public class EmailVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * 원문 토큰 저장 금지 권장: hash만 저장
     */
    @Column(name = "token_hash", nullable = false, length = 1024)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 미사용이면 null, 사용 처리 시각을 기록
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * 메일 발송 성공 시각 (실패/테스트 등 고려해 null 허용 권장)
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "request_ip", length = 255)
    private String requestIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    protected EmailVerificationToken(
            String email,
            String tokenHash,
            LocalDateTime expiresAt,
            String requestIp,
            String userAgent
    ) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.requestIp = requestIp;
        this.userAgent = userAgent;
    }

    public static EmailVerificationToken issue(
            String email,
            String tokenHash,
            LocalDateTime expiresAt,
            String requestIp,
            String userAgent
    ) {
        return new EmailVerificationToken(email, tokenHash, expiresAt, requestIp, userAgent);
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now) || expiresAt.isEqual(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void markUsed(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
