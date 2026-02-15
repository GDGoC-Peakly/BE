package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    boolean existsByUser_IdAndSessionStatusIn(Long userId, Iterable<SessionStatus> statuses);
}
