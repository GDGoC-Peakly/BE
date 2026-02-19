package com.example.peakly.domain.focusSession.entity;

import com.example.peakly.domain.focusSession.command.FocusSessionStartCommand;
import com.example.peakly.domain.report.util.ReportingDateUtil;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "focus_sessions",
        indexes = {
                @Index(name = "idx_fs_user_base_date", columnList = "user_id, base_date"),
                @Index(name = "idx_fs_user_started_at", columnList = "user_id, started_at"),
                @Index(name = "idx_fs_user_status", columnList = "user_id, session_status")
        }
)
public class FocusSession extends BaseEntity {

    private static final LocalTime REPORT_CUTOFF = LocalTime.of(5, 0); //5시 컷오프

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "focus_session_id")
    private Long id;

    @Column(name = "major_category_id", nullable = false)
    private Long majorCategoryId;

    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 20)
    private SessionStatus sessionStatus;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "goal_duration_sec", nullable = false)
    private int goalDurationSec;

    @Column(name = "fatigue_level", nullable = false)
    private byte fatigueLevel;

    @Column(name = "caffeine_intake_level", nullable = false)
    private byte caffeineIntakeLevel;

    @Column(name = "noise_level", nullable = false)
    private byte noiseLevel;

    @Column(name = "total_focus_sec", nullable = false)
    private int totalFocusSec;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "is_counted_in_stats", nullable = false)
    private boolean countedInStats;

    @Column(name = "is_recorded", nullable = false)
    private boolean recorded;

    @Version
    private Long version;

    @OneToMany(mappedBy = "focusSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SessionPause> pauses = new ArrayList<>();

    @OneToOne(mappedBy = "focusSession", fetch = FetchType.LAZY)
    private PeaktimeFeedback peaktimeFeedback;

    public static FocusSession start(
            User user,
            FocusSessionStartCommand cmd,
            LocalDateTime startedAt,
            LocalDate baseDate
    ) {
        validateStart(user, cmd, startedAt, baseDate);

        FocusSession s = new FocusSession();
        s.user = user;

        s.majorCategoryId = cmd.majorCategoryId();
        s.categoryId = cmd.categoryId();

        s.startedAt = startedAt;
        s.baseDate = baseDate;

        s.goalDurationSec = cmd.goalDurationSec();
        s.fatigueLevel = (byte) cmd.fatigueLevel();
        s.caffeineIntakeLevel = (byte) cmd.caffeineIntakeLevel();
        s.noiseLevel = (byte) cmd.noiseLevel();

        s.sessionStatus = SessionStatus.RUNNING;
        s.totalFocusSec = 0;
        s.countedInStats = false;

        s.recorded = false;
        return s;
    }

    public void markRecorded(boolean recorded) {
        this.recorded = recorded;
    }

    private static void validateStart(
            User user,
            FocusSessionStartCommand cmd,
            LocalDateTime startedAt,
            LocalDate baseDate
    ) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (cmd == null) throw new IllegalArgumentException("cmd는 필수입니다.");

        if (cmd.majorCategoryId() == null) throw new IllegalArgumentException("majorCategoryId는 필수입니다.");
        if (startedAt == null) throw new IllegalArgumentException("startedAt은 필수입니다.");
        if (baseDate == null) throw new IllegalArgumentException("baseDate는 필수입니다.");

        if (cmd.goalDurationSec() <= 0) throw new IllegalArgumentException("goalDurationSec는 0보다 커야 합니다.");
        if (cmd.goalDurationSec() > 86400) throw new IllegalArgumentException("goalDurationSec는 86400초(24시간) 이하여야 합니다.");

        if (cmd.fatigueLevel() < 1 || cmd.fatigueLevel() > 5)
            throw new IllegalArgumentException("fatigueLevel은 1~5 범위여야 합니다.");
        if (cmd.caffeineIntakeLevel() < 0 || cmd.caffeineIntakeLevel() > 2)
            throw new IllegalArgumentException("caffeineIntakeLevel은 0~2 범위여야 합니다.");
        if (cmd.noiseLevel() < 0 || cmd.noiseLevel() > 2)
            throw new IllegalArgumentException("noiseLevel은 0~2 범위여야 합니다.");
    }

    public void pause(LocalDateTime pausedAt) {
        if (pausedAt == null) throw new IllegalArgumentException("pausedAt은 필수입니다.");
        if (this.sessionStatus != SessionStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태에서만 일시정지가 가능합니다.");
        }
        this.sessionStatus = SessionStatus.PAUSED;
        this.pauses.add(SessionPause.create(this, pausedAt));
    }

    public void addFocusSec(int deltaSec) {
        if (deltaSec < 0) throw new IllegalArgumentException("deltaSec는 0 이상이어야 합니다.");
        this.totalFocusSec += deltaSec;
    }

    public void markRunning() {
        if (this.sessionStatus != SessionStatus.PAUSED) {
            throw new IllegalStateException("PAUSED 상태에서만 RUNNING으로 바꿀 수 있습니다.");
        }
        this.sessionStatus = SessionStatus.RUNNING;
    }

    public void end(LocalDateTime endedAt, int countedThresholdSec) {
        if (endedAt == null) throw new IllegalArgumentException("endedAt은 필수입니다.");
        if (countedThresholdSec < 0) throw new IllegalArgumentException("countedThresholdSec는 0 이상이어야 합니다.");

        if (this.sessionStatus == SessionStatus.ENDED || this.sessionStatus == SessionStatus.CANCELED) {
            throw new IllegalStateException("이미 종료 또는 취소된 세션입니다.");
        }

        this.endedAt = endedAt;
        this.sessionStatus = SessionStatus.ENDED;

        this.baseDate = resolveBaseDateByCutoff(endedAt); //종료 시점 기준으로 baseDate 최종 확정

        this.countedInStats = this.totalFocusSec >= countedThresholdSec;
    }

    public void cancel(LocalDateTime canceledAt) {
        if (canceledAt == null) throw new IllegalArgumentException("canceledAt은 필수입니다.");
        if (this.sessionStatus == SessionStatus.ENDED) {
            throw new IllegalStateException("종료된 세션은 취소할 수 없습니다.");
        }

        this.endedAt = canceledAt;
        this.sessionStatus = SessionStatus.CANCELED;

        this.baseDate = resolveBaseDateByCutoff(canceledAt);
    }

    private static LocalDate resolveBaseDateByCutoff(LocalDateTime time) {
        LocalDate d = time.toLocalDate();
        LocalTime t = time.toLocalTime();
        // 05:00 "이전"이면 전날로 귀속 (04:59:59 포함)
        if (t.isBefore(REPORT_CUTOFF)) return d.minusDays(1);
        return d;
    }
}
