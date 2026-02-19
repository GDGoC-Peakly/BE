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

    @Column(name = "total_target_sec", nullable = false)
    private Integer totalTargetSec = 0;

    @Column(name = "achievement_rate", nullable = false)
    private Double achievementRate = 0.0;

    @Column(name = "accuracy_rate", nullable = false)
    private Double accuracyRate = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight", length = 255)
    private Insight insight;

    // 주간/월간 집계 기준일(ex-그 주의 월요일 / 그 달의 1일 )
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void syncWeekday() {
        if (this.reportDate == null) return;
        this.weekday = Weekday.from(this.reportDate.getDayOfWeek());
    }

    //리포트 생성
    public static DailyReport create(User user, LocalDate date) {
        DailyReport report = new DailyReport();
        report.user = user;
        report.reportDate = date;
        report.baseDate = date.minusDays(date.getDayOfWeek().getValue() - 1); // 그 주 월요일
        return report;
    }

    // 업데이트
    public void update(int totalFocusSec, int totalTargetSec,
                       double achievementRate, double accuracyRate, Insight insight) {
        this.totalFocusSec = totalFocusSec;
        this.totalTargetSec = totalTargetSec;
        this.achievementRate = achievementRate;
        this.accuracyRate = accuracyRate;
        this.insight = insight;
    }
}

