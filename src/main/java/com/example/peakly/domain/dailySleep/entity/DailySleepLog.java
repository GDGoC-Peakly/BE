package com.example.peakly.domain.dailySleep.entity;

import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "daily_sleep_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_daily_sleep_user_date",
                        columnNames = {"user_id", "base_date"}
                )
        },
        indexes = {
                // "하루 수면 체크인 조회"
                @Index(name = "idx_sleep_user_date", columnList = "user_id, base_date")
        }
)
public class DailySleepLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_sleep_id")
    private Long id;

    // 기준 날짜(해당 날짜의 수면 기록)
    @NotNull
    @PastOrPresent  // 미래 날짜 저장 방지
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @NotNull
    @Column(name = "bed_time", nullable = false)
    private LocalTime bedTime; //취침시간

    @NotNull
    @Column(name = "wake_time", nullable = false)
    private LocalTime wakeTime; //기상시간

    // 수면 시간(분)
    @NotNull
    @Min(0)                 // 음수 방지
    @Max(24 * 60)           // 1440분 초과 방지
    @Column(name = "sleep_duration_min", nullable = false)
    private Integer sleepDurationMin;

    // 수면 점수
    @NotNull
    @Min(0)
    @Max(5)
    @Column(name = "sleep_score", nullable = false)
    private Integer sleepScore;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 수정 메서드 추가
    public void updateLog(LocalTime bedTime, LocalTime wakeTime, Integer duration, Integer sleepScore) {
        this.bedTime = bedTime;
        this.wakeTime = wakeTime;
        this.sleepDurationMin = duration;
        this.sleepScore = sleepScore;
    }
}