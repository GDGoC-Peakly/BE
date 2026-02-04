package com.example.peakly.domain.report.entity;
import com.example.peakly.domain.report.enums.Insight;
import com.example.peakly.domain.report.enums.Weekday;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "daily_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_daily_reports_user_report_date",
                        columnNames = {"user_id", "report_date"}
                )
        },
        indexes = {
                @Index(name = "idx_daily_reports_user_base_date", columnList = "user_id, base_date")
        }
)
public class DailyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_id")
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private Weekday weekday;

    @Column(name = "total_focus_sec", nullable = false)
    private Integer totalFocusSec = 0;

    @Column(name = "achievement_rate")
    private Float achievementRate;

    @Column(name = "accuracy_rate", nullable = false)
    private Float accuracyRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight", length = 255)
    private Insight insight;

    // 주간/월간 집계 기준일(ex-그 주의 월요일 / 그 달의 1일 )
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

