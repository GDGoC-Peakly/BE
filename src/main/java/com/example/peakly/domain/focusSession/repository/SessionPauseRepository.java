package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.SessionPause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionPauseRepository extends JpaRepository<SessionPause, Long> {

    boolean existsByFocusSession_IdAndResumedAtIsNull(Long sessionId);

    @Query("SELECT sp FROM SessionPause sp" +
            " WHERE sp.focusSession.id = :sessionId" +
            " AND sp.resumedAt IS NOT NULL" +
            " ORDER BY sp.resumedAt DESC" +
            " LIMIT 1")
    Optional<SessionPause> findLatestResumedPause(Long sessionId);

    List<SessionPause> findAllByFocusSession_IdAndResumedAtIsNull(Long sessionId);
}