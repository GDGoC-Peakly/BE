package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.SessionPause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionPauseRepository extends JpaRepository<SessionPause, Long> {

    boolean existsByFocusSession_IdAndResumedAtIsNull(Long sessionId);

    Optional<SessionPause> findTopByFocusSession_IdOrderByPausedAtDesc(Long sessionId);
}