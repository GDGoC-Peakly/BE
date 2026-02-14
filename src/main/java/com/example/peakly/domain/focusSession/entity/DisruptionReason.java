package com.example.peakly.domain.focusSession.entity;

import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "disruption_reasons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_disruption_reason_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_disruption_reason_sort", columnList = "sort_order")
        }
)
public class DisruptionReason extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reason_id")
    private Long id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public static DisruptionReason create(String code, String name, int sortOrder) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code는 필수입니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name은 필수입니다.");
        if (sortOrder < 0) throw new IllegalArgumentException("sortOrder는 0 이상이어야 합니다.");

        DisruptionReason r = new DisruptionReason();
        r.code = code;
        r.name = name;
        r.sortOrder = sortOrder;
        r.active = true;
        return r;
    }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
}
