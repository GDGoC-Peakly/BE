package com.example.peakly.domain.focus.entity;

import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "session_disruption_reasons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_feedback_reason", columnNames = {"feedback_id", "reason_id"})
        },
        indexes = {
                @Index(name = "idx_sdr_feedback", columnList = "feedback_id"),
                @Index(name = "idx_sdr_reason", columnList = "reason_id")
        }
)
public class SessionDisruptionReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_disruption_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private PeaktimeFeedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason_id", nullable = false)
    private DisruptionReason reason;

    public static SessionDisruptionReason link(PeaktimeFeedback feedback, DisruptionReason reason) {
        if (feedback == null) throw new IllegalArgumentException("feedback은 필수입니다.");
        if (reason == null) throw new IllegalArgumentException("reason은 필수입니다.");

        SessionDisruptionReason l = new SessionDisruptionReason();
        l.feedback = feedback;
        l.reason = reason;
        return l;
    }
}
