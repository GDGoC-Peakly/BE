package com.example.peakly.domain.auth.entity;

import com.example.peakly.domain.auth.command.AuthSessionIssueCommand;
import com.example.peakly.domain.user.entity.User;
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
        name = "auth_session",
        indexes = {
                @Index(name = "idx_auth_session_user", columnList = "user_id"),
                @Index(name = "idx_auth_session_token", columnList = "refresh_token_hash")
        }
)
public class AuthSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId;

    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    private String refreshTokenHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "rotated_at")
    private LocalDateTime rotatedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    public static AuthSession issue(User user, AuthSessionIssueCommand cmd) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (cmd == null) throw new IllegalArgumentException("cmd는 필수입니다.");

        if (cmd.deviceId() == null || cmd.deviceId().isBlank()) {
            throw new IllegalArgumentException("deviceId는 필수입니다.");
        }
        if (cmd.refreshTokenHash() == null || cmd.refreshTokenHash().length() != 64) {
            throw new IllegalArgumentException("refreshTokenHash는 64자여야 합니다.");
        }
        if (cmd.issuedAt() == null) throw new IllegalArgumentException("issuedAt은 필수입니다.");
        if (cmd.expiresAt() == null) throw new IllegalArgumentException("expiresAt은 필수입니다.");

        AuthSession s = new AuthSession();
        s.user = user;
        s.deviceId = cmd.deviceId();
        s.refreshTokenHash = cmd.refreshTokenHash();
        s.issuedAt = cmd.issuedAt();
        s.expiresAt = cmd.expiresAt();
        return s;
    }

    public void markLastSeen(LocalDateTime now) {
        if (now == null) throw new IllegalArgumentException("now는 필수입니다.");
        this.lastSeenAt = now;
    }

    public void revoke(LocalDateTime now) {
        if (now == null) throw new IllegalArgumentException("now는 필수입니다.");
        this.revokedAt = now;
    }

    public void rotate(String newRefreshTokenHash, LocalDateTime now) {
        if (newRefreshTokenHash == null || newRefreshTokenHash.length() != 64) {
            throw new IllegalArgumentException("newRefreshTokenHash는 64자여야 합니다.");
        }
        if (now == null) throw new IllegalArgumentException("now는 필수입니다.");
        this.refreshTokenHash = newRefreshTokenHash;
        this.rotatedAt = now;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
