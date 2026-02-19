package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.PeaktimeFeedback;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PeaktimeFeedbackRepository extends JpaRepository<PeaktimeFeedback, Long> {
    boolean existsByFocusSession_Id(Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM PeaktimeFeedback f WHERE f.focusSession.id = :sessionId")
    Optional<PeaktimeFeedback> findByFocusSessionIdForUpdate(@Param("sessionId") Long sessionId);
}
