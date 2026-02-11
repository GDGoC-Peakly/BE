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
        name = "device_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_device_token_fcm", columnNames = {"fcm_token"})
        },
        indexes = {
                @Index(name = "idx_device_token_user", columnList = "user_id")
        }
)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static DeviceToken of(User user, String fcmToken) {
        DeviceToken dt = new DeviceToken();
        dt.user = user;
        dt.fcmToken = fcmToken;
        return dt;
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}