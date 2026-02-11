package com.example.peakly.domain.user.entity;

import com.example.peakly.domain.auth.entity.AuthSession;
import com.example.peakly.domain.notification.entity.UserNotificationSetting;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "job", length = 30)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus userStatus;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "purge_at")
    private LocalDateTime purgeAt;

    // 연관관계 (편의상 양방향)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private InitialData initialData;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private final List<AuthSession> authSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private final List<UserNotificationSetting> notificationSettings = new ArrayList<>();

    protected User(
            String email,
            String passwordHash,
            String nickname,
            Job job,
            AuthProvider provider,
            String providerId,
            UserStatus userStatus
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.job = job;
        this.provider = provider;
        this.providerId = providerId;
        this.userStatus = userStatus;
    }

    public static User createEmailUser(String email, String passwordHash, String nickname) {
        return new User(email, passwordHash, nickname, null, AuthProvider.EMAIL, null, UserStatus.ACTIVE);
    }

    public void updateJob(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("job은 필수입니다.");
        }
        this.job = job;
    }
}
