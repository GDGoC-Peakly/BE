package com.example.peakly.domain.auth.repository;

import com.example.peakly.domain.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    boolean existsByEmailAndUsedAtAfter(String email, LocalDateTime usedAfter);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}