package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findByIdAndUser_Id(Long sessionId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM FocusSession s WHERE s.id = :sessionId AND s.user.id = :userId")
    Optional<FocusSession> findByIdAndUserIdForUpdate(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId
    );


    boolean existsByUser_IdAndSessionStatusIn(Long userId, Iterable<SessionStatus> statuses);

    @Query("""
        select fs
        from FocusSession fs
        where fs.user.id = :userId
          and fs.sessionStatus in :statuses
        order by fs.startedAt desc
    """)
    List<FocusSession> findActiveSessions(
            @Param("userId") Long userId,
            @Param("statuses") List<SessionStatus> statuses,
            Pageable pageable
    );

    List<FocusSession> findByUser_IdAndBaseDateAndSessionStatus(Long userId, LocalDate baseDate, SessionStatus status);

    List<FocusSession> findByUser_IdAndBaseDateBetweenAndSessionStatus(
            Long userId,
            LocalDate from,
            LocalDate to,
            SessionStatus sessionStatus
    );
}
