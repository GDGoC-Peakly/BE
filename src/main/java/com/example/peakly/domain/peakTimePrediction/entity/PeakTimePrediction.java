package com.example.peakly.domain.peakTimePrediction.entity;

import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "peaktime_predictions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_peak_user_date",
                        columnNames = {"user_id", "base_date"}
                )
        },
        indexes = {
                @Index(name = "idx_peak_user_date", columnList = "user_id, base_date")
        }
)
public class PeakTimePrediction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long id;

    @NotNull
    @PastOrPresent
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @NotNull
    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PeakTimePredictionWindow> windows = new ArrayList<>();

    public static PeakTimePrediction create(User user, LocalDate baseDate, String modelVersion, LocalDateTime computedAt) {
        if (user == null) throw new IllegalArgumentException("user는 필수입니다.");
        if (baseDate == null) throw new IllegalArgumentException("baseDate는 필수입니다.");
        if (modelVersion == null || modelVersion.isBlank()) throw new IllegalArgumentException("modelVersion은 필수입니다.");
        if (computedAt == null) throw new IllegalArgumentException("computedAt은 필수입니다.");

        PeakTimePrediction p = new PeakTimePrediction();
        p.user = user;
        p.baseDate = baseDate;
        p.modelVersion = modelVersion;
        p.computedAt = computedAt;
        return p;
    }

    public void replaceWindows(List<PeakTimePredictionWindow> newWindows) {
        this.windows.clear();
        if (newWindows != null) {
            this.windows.addAll(newWindows);
        }
    }

    public void updateComputedAt(LocalDateTime computedAt) {
        if (computedAt == null) throw new IllegalArgumentException("computedAt은 필수입니다.");
        this.computedAt = computedAt;
    }

    public void updateModelVersion(String modelVersion) {
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("modelVersion은 필수입니다.");
        }
        this.modelVersion = modelVersion;
    }
}