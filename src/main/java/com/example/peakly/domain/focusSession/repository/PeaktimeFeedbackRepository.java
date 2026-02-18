package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.PeaktimeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeaktimeFeedbackRepository extends JpaRepository<PeaktimeFeedback, Long> {
    boolean existsByFocusSession_Id(Long sessionId);
    Optional<PeaktimeFeedback> findByFocusSession_Id(Long sessionId);
}
