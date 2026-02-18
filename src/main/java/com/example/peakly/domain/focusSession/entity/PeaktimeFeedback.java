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

        int score = cmd.focusScore();
        if (score < 1 || score > 5) throw new IllegalArgumentException("focusScore는 1~5 범위여야 합니다.");

        PeaktimeFeedback f = new PeaktimeFeedback();
        f.focusSession = focusSession;
        f.focusScore = (byte) cmd.focusScore();
        return f;
    }

    public void addDisruptionReason(DisruptionReason reason) {
        if (reason == null) throw new IllegalArgumentException("reason은 필수입니다.");
        this.disruptionReasons.add(SessionDisruptionReason.link(this, reason));
    }
}
