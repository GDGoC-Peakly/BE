package com.example.peakly.domain.term.entity;

import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user_term",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_term_user_term",
                        columnNames = {"user_id", "term_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_term_user", columnList = "user_id"),
                @Index(name = "idx_user_term_term", columnList = "term_id")
        }
)
public class UserTerm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_term_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Terms term;

    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed = true;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime updateAt;

    public static UserTerm agree(User user, Terms term, LocalDateTime updateAt) {
        java.util.Objects.requireNonNull(user, "user");
        java.util.Objects.requireNonNull(term, "term");
        UserTerm ut = new UserTerm();
        ut.user = user;
        ut.term = term;
        ut.isAgreed = true;
        ut.updateAt = (updateAt != null ? updateAt : LocalDateTime.now());
        return ut;
    }

    public void withdrawAgreement() {
        this.isAgreed = false;
        this.updateAt = LocalDateTime.now();
    }
}

