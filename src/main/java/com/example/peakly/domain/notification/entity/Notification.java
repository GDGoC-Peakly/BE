package com.example.peakly.domain.notification.entity;

import com.example.peakly.domain.notification.enums.NotificationType;
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
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user", columnList = "user_id"),
                @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
                @Index(name = "idx_notification_type", columnList = "notification_type")
        }
)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public static Notification create(User user, String title, String body, NotificationType type
    ) {
        Notification n = new Notification();
        n.user = user;
        n.title = title;
        n.body = body;
        n.notificationType = type;
        n.isRead = false;
        return n;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
