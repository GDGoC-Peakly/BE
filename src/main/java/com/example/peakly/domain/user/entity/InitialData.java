package com.example.peakly.domain.user.entity;

import com.example.peakly.domain.user.command.InitialDataCreateCommand;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "initial_data")
public class InitialData extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "chronotype", nullable = false, length = 20)
    private Chronotype chronotype;

    @Enumerated(EnumType.STRING)
    @Column(name = "subjective_peaktime", nullable = false, length = 20)
    private SubjectivePeaktime subjectivePeaktime;

    @Column(name = "caffeine_responsiveness", nullable = false)
    private byte caffeineResponsiveness; // 0~2

    @Column(name = "noise_sensitivity", nullable = false)
    private byte noiseSensitivity; // 0~2

    public static InitialData create(User user, InitialDataCreateCommand cmd) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (cmd == null) throw new IllegalArgumentException("cmd는 필수입니다.");
        if (cmd.chronotype() == null) throw new IllegalArgumentException("chronotype은 필수입니다.");

        if (cmd.subjectivePeaktime() == null) {
            throw new IllegalArgumentException("subjectivePeaktime은 필수입니다.");
        }

        if (cmd.caffeineResponsiveness() < 0 || cmd.caffeineResponsiveness() > 2) {
            throw new IllegalArgumentException("caffeineResponsiveness는 0~2 범위여야 합니다.");
        }
        if (cmd.noiseSensitivity() < 0 || cmd.noiseSensitivity() > 2) {
            throw new IllegalArgumentException("noiseSensitivity는 0~2 범위여야 합니다.");
        }

        InitialData d = new InitialData();
        d.user = user;
        d.chronotype = cmd.chronotype();
        d.subjectivePeaktime = cmd.subjectivePeaktime();
        d.caffeineResponsiveness = (byte) cmd.caffeineResponsiveness();
        d.noiseSensitivity = (byte) cmd.noiseSensitivity();
        return d;
    }
}
