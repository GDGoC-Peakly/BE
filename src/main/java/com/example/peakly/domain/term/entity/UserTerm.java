package com.example.peakly.domain.term.entity;

import com.example.peakly.domain.user.entity.User;
import com.example.peakly.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private LocalDateTime agreedAt;

    /**
     * Creates a new UserTerm recording that the given user agreed to the specified term.
     *
     * @param user the user who agreed to the term
     * @param term the term that was agreed to
     * @param agreedAt the timestamp when the agreement occurred; if null, the current time is used
     * @return a new UserTerm with `isAgreed` set to `true` and `agreedAt` initialized
     */
    public static UserTerm agree(User user, Terms term, LocalDateTime agreedAt) {
        UserTerm ut = new UserTerm();
        ut.user = user;
        ut.term = term;
        ut.isAgreed = true;
        ut.agreedAt = (agreedAt != null ? agreedAt : LocalDateTime.now());
        return ut;
    }

    /**
     * Marks the user's agreement as withdrawn and records the withdrawal time.
     *
     * Sets the agreement flag to `false` and updates `agreedAt` to the current time.
     */
    public void withdrawAgreement() {
        this.isAgreed = false;
        this.agreedAt = LocalDateTime.now();
    }
}
