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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="window_json", columnDefinition="json", nullable=false)
    private String windowJson;

    @NotNull
    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @NotNull
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
