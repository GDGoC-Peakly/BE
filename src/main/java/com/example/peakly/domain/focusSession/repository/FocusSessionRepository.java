package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findByIdAndUser_Id(Long sessionId, Long userId);

    boolean existsByUser_IdAndSessionStatusIn(Long userId, Iterable<SessionStatus> statuses);
}
