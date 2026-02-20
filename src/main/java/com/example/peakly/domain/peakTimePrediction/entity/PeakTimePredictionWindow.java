package com.example.peakly.domain.peakTimePrediction.entity;

import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "peaktime_prediction_windows",
        indexes = {
                @Index(name = "idx_peak_win_pred_start", columnList = "prediction_id, start_minute_of_day")
        }
)
public class PeakTimePredictionWindow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "window_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prediction_id", nullable = false)
    private PeakTimePrediction prediction;

    @Column(name = "rank_no", nullable = false)
    private int rank;

    @Column(name = "start_minute_of_day", nullable = false)
    private int startMinuteOfDay; // 0~1439

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;  // >0

    @Column(name = "score_raw", nullable = false)
    private double scoreRaw;

    @Column(name = "score_01", nullable = false)
    private double score01;       // 0~1

    public static PeakTimePredictionWindow create(
            PeakTimePrediction prediction,
            int rank,
            int startMinuteOfDay,
            int durationMinutes,
            double scoreRaw,
            double score01
    ) {
        if (prediction == null) throw new IllegalArgumentException("prediction은 필수입니다.");
        if (rank <= 0) throw new IllegalArgumentException("rank는 1 이상이어야 합니다.");
        if (startMinuteOfDay < 0 || startMinuteOfDay > 1439) {
            throw new IllegalArgumentException("startMinuteOfDay는 0~1439 범위여야 합니다.");
        }
        if (durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes는 1 이상이어야 합니다.");
        if (score01 < 0.0 || score01 > 1.0) throw new IllegalArgumentException("score01은 0~1 범위여야 합니다.");

        PeakTimePredictionWindow w = new PeakTimePredictionWindow();
        w.prediction = prediction;
        w.rank = rank;
        w.startMinuteOfDay = startMinuteOfDay;
        w.durationMinutes = durationMinutes;
        w.scoreRaw = scoreRaw;
        w.score01 = score01;
        return w;
    }

    public static PeakTimePredictionWindow fromAi(
            PeakTimePrediction prediction,
            int rank,
            double hour,
            double durationHours,
            double scoreRaw,
            double maxRaw
    ) {
        if (prediction == null) throw new IllegalArgumentException("prediction은 필수입니다.");
        if (rank <= 0) throw new IllegalArgumentException("rank는 1 이상이어야 합니다.");
        if (hour < 0.0 || hour > 24.0) throw new IllegalArgumentException("hour 범위 오류");
        if (durationHours <= 0.0) throw new IllegalArgumentException("durationHours는 0보다 커야 합니다.");

        int startMinuteOfDay = (int) Math.round(hour * 60.0);
        int durationMinutes = (int) Math.round(durationHours * 60.0);

        if (startMinuteOfDay < 0) startMinuteOfDay = 0;
        if (startMinuteOfDay > 24 * 60) startMinuteOfDay = 24 * 60;
        if (durationMinutes <= 0) durationMinutes = 1;

        double denom = (maxRaw > 0.0) ? maxRaw : 1.0;
        double score01 = scoreRaw / denom;
        if (score01 < 0.0) score01 = 0.0;
        if (score01 > 1.0) score01 = 1.0;

        PeakTimePredictionWindow w = new PeakTimePredictionWindow();
        w.prediction = prediction;
        w.rank = rank;
        w.startMinuteOfDay = startMinuteOfDay;
        w.durationMinutes = durationMinutes;
        w.scoreRaw = scoreRaw;
        w.score01 = score01;
        return w;
    }
}
