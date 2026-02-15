package com.example.peakly.domain.focusSession.entity;

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
        name = "session_pauses",
        indexes = {
                @Index(name = "idx_pause_session", columnList = "focus_session_id")
        }
)
public class SessionPause extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_pause_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "focus_session_id", nullable = false)
    private FocusSession focusSession;

    @Column(name = "paused_at", nullable = false)
    private LocalDateTime pausedAt;

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @Column(name = "pause_sec")
    private Integer pauseSec;

    public static SessionPause create(FocusSession focusSession, LocalDateTime pausedAt) {
        if (focusSession == null) throw new IllegalArgumentException("focusSession은 필수입니다.");
        if (pausedAt == null) throw new IllegalArgumentException("pausedAt은 필수입니다.");

        SessionPause p = new SessionPause();
        p.focusSession = focusSession;
        p.pausedAt = pausedAt;
        return p;
    }

    public void resume(LocalDateTime resumedAt, int pauseSec) {
        if (resumedAt == null) throw new IllegalArgumentException("resumedAt은 필수입니다.");
        if (pauseSec < 0) throw new IllegalArgumentException("pauseSec는 0 이상이어야 합니다.");
        if (this.resumedAt != null) throw new IllegalStateException("이미 resume 처리된 pause입니다.");

        this.resumedAt = resumedAt;
        this.pauseSec = pauseSec;
    }

    public boolean isOpen() {
        return resumedAt == null;
    }
}
