package com.example.peakly.domain.notification.entity;

import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user_notification_settings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_notification",
                        columnNames = {"user_id", "notification_type"}
                )
        },
        indexes = {
                @Index(name = "idx_user_notification_user", columnList = "user_id")
        }
)
public class UserNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    public static UserNotificationSetting create(User user, NotificationType type, boolean enabled) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (type == null) throw new IllegalArgumentException("notificationType은 필수입니다.");

        UserNotificationSetting s = new UserNotificationSetting();
        s.user = user;
        s.notificationType = type;
        s.enabled = enabled;
        return s;
    }

    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
}
