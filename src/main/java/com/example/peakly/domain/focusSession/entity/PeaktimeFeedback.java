package com.example.peakly.domain.focusSession.entity;

import com.example.peakly.domain.focusSession.command.PeaktimeFeedbackCreateCommand;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "peaktime_feedback",
        uniqueConstraints = @UniqueConstraint(
                name="uk_pf_session", columnNames={"focus_session_id"}),
        indexes = {
                @Index(name = "idx_feedback_session", columnList = "focus_session_id")
        }
)
public class PeaktimeFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "focus_session_id", nullable = false /*, unique = true*/)
    private FocusSession focusSession;

    @Column(name = "focus_score", nullable = false)
    private byte focusScore;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<SessionDisruptionReason> disruptionReasons = new ArrayList<>();

    public static PeaktimeFeedback create(FocusSession focusSession, PeaktimeFeedbackCreateCommand cmd) {
        if (focusSession == null) throw new IllegalArgumentException("focusSession은 필수입니다.");
        if (cmd == null) throw new IllegalArgumentException("cmd는 필수입니다.");

        // 점수 범위는 ERD/SQL 확정치가 아니면 강제하지 않는 게 안전합니다.
        // 여기서는 '음수 금지' 정도만 걸어둡니다.
        if (cmd.focusScore() < 0) throw new IllegalArgumentException("focusScore는 0 이상이어야 합니다.");

        PeaktimeFeedback f = new PeaktimeFeedback();
        f.focusSession = focusSession;
        f.focusScore = (byte) cmd.focusScore();

        if (cmd.disruptionReasonIds() != null) {
            // 이유 ID 리스트는 서비스 계층에서 DisruptionReason을 조회한 뒤 link 하시는 게 안전합니다.
            // (엔티티 내부에서 repository 접근 금지)
        }

        return f;
    }

    public void addDisruptionReason(DisruptionReason reason) {
        if (reason == null) throw new IllegalArgumentException("reason은 필수입니다.");
        this.disruptionReasons.add(SessionDisruptionReason.link(this, reason));
    }
}
