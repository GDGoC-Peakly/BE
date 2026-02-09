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

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "sent_at", nullable = true)
    private LocalDateTime sentAt;

    @Column(name = "request_ip", length = 255, nullable = true)
    private String requestIp;

    @Column(name = "user_agent", length = 1024, nullable = true)
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
        if (this.usedAt != null) return;
        this.usedAt = usedAt;
    }
}
