package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.category.entity.Category;
import com.example.peakly.domain.category.entity.MajorCategory;
import com.example.peakly.domain.category.repository.CategoryRepository;
import com.example.peakly.domain.category.repository.MajorCategoryRepository;
import com.example.peakly.domain.focusSession.command.FocusSessionStartCommand;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionEndResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionPauseResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionResumeResponse;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionStartResponse;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.focusSession.repository.SessionPauseRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.CategoryErrorStatus;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final MajorCategoryRepository majorCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SessionPauseRepository sessionPauseRepository;

    @Transactional
    public FocusSessionStartResponse start(Long userId, FocusSessionStartRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        boolean exists = focusSessionRepository.existsByUser_IdAndSessionStatusIn(
                userId,
                List.of(SessionStatus.RUNNING, SessionStatus.PAUSED)
        );
        if (exists) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_ALREADY_RUNNING);
        }

        MajorCategory major = majorCategoryRepository.findById(req.majorCategoryId())
                .orElseThrow(() -> new GeneralException(CategoryErrorStatus.MAJOR_CATEGORY_NOT_FOUND));

        if (req.categoryId() != null) {
            Category category = categoryRepository.findByIdAndUser_Id(req.categoryId(), userId)
                    .orElseThrow(() -> new GeneralException(CategoryErrorStatus.CATEGORY_NOT_FOUND));

            Long categoryMajorId = category.getMajorCategory().getId();
            if (!categoryMajorId.equals(major.getId())) {
                throw new GeneralException(CategoryErrorStatus.CATEGORY_MAJOR_MISMATCH);
            }
        }

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDate baseDate = calcBaseDate(startedAt);

        FocusSessionStartCommand cmd = new FocusSessionStartCommand(
                req.majorCategoryId(),
                req.categoryId(),
                req.goalDurationSec(),
                req.fatigueLevel(),
                req.caffeineIntakeLevel(),
                req.noiseLevel()
        );

        FocusSession session = FocusSession.start(user, cmd, startedAt, baseDate);
        FocusSession saved = focusSessionRepository.save(session);

        LocalDateTime expectedEndAt = startedAt.plusSeconds(req.goalDurationSec());

        return new FocusSessionStartResponse(
                saved.getId(),
                saved.getSessionStatus().name(),
                saved.getStartedAt(),
                saved.getBaseDate(),
                saved.getGoalDurationSec(),
                expectedEndAt
        );
    }

    private LocalDate calcBaseDate(LocalDateTime startedAt) {
        LocalTime dayStart = LocalTime.of(5, 0);
        if (startedAt.toLocalTime().isBefore(dayStart)) {
            return startedAt.toLocalDate().minusDays(1);
        }
        return startedAt.toLocalDate();
    }

    @Transactional
    public FocusSessionPauseResponse pause(Long userId, Long sessionId) {
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.RUNNING) {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        if (sessionPauseRepository.existsByFocusSession_IdAndResumedAtIsNull(sessionId)) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }

        LocalDateTime pausedAt = LocalDateTime.now();

        LocalDateTime lastRunningStartedAt = sessionPauseRepository
                .findTopByFocusSession_IdAndResumedAtIsNotNullOrderByResumedAtDesc(sessionId)
                .map(SessionPause::getResumedAt)
                .orElse(session.getStartedAt());

        long deltaSec = Duration.between(lastRunningStartedAt, pausedAt).getSeconds();
        if (deltaSec < 0) throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);

        session.addFocusSec((int) deltaSec);

        session.pause(pausedAt);

        SessionPause created = session.getPauses().get(session.getPauses().size() - 1);
        sessionPauseRepository.save(created);

        return new FocusSessionPauseResponse(
                session.getId(),
                session.getSessionStatus().name(),
                new FocusSessionPauseResponse.PauseDTO(
                        created.getId(),
                        created.getPausedAt()
                )
        );
    }

    @Transactional
    public FocusSessionResumeResponse resume(Long userId, Long sessionId) {
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.PAUSED) {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        List<SessionPause> openPauses =
                sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId);

        if (openPauses.size() != 1) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }

        SessionPause open = openPauses.get(0);

        LocalDateTime resumedAt = LocalDateTime.now();
        long pauseSecLong = Duration.between(open.getPausedAt(), resumedAt).getSeconds();
        if (pauseSecLong < 0) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }

        open.resume(resumedAt, (int) pauseSecLong);

        session.markRunning();

        int remainingFocusSec = Math.max(session.getGoalDurationSec() - session.getTotalFocusSec(), 0);
        LocalDateTime expectedEndAt = resumedAt.plusSeconds(remainingFocusSec);

        return new FocusSessionResumeResponse(
                session.getId(),
                session.getSessionStatus().name(),
                session.getTotalFocusSec(),
                expectedEndAt,
                new FocusSessionResumeResponse.PauseDTO(
                        open.getId(),
                        open.getPausedAt(),
                        open.getResumedAt(),
                        open.getPauseSec()
                )
        );
    }

    @Transactional
    public FocusSessionEndResponse end(Long userId, Long sessionId, FocusSessionEndRequest req) {

        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() == SessionStatus.ENDED || session.getSessionStatus() == SessionStatus.CANCELED) {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        LocalDateTime endedAt = LocalDateTime.now();

        if (session.getSessionStatus() == SessionStatus.RUNNING) {

            if (sessionPauseRepository.existsByFocusSession_IdAndResumedAtIsNull(sessionId)) {
                throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
            }

            LocalDateTime lastRunningStartedAt = sessionPauseRepository
                    .findTopByFocusSession_IdAndResumedAtIsNotNullOrderByResumedAtDesc(sessionId)
                    .map(SessionPause::getResumedAt)
                    .orElse(session.getStartedAt());

            long deltaSec = Duration.between(lastRunningStartedAt, endedAt).getSeconds();
            if (deltaSec < 0) throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);

            session.addFocusSec((int) deltaSec);
            session.end(endedAt, session.getTotalFocusSec());

        } else if (session.getSessionStatus() == SessionStatus.PAUSED) {

            List<SessionPause> openPauses =
                    sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId);

            if (openPauses.size() != 1) {
                throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
            }

            SessionPause open = openPauses.get(0);

            long pauseSecLong = Duration.between(open.getPausedAt(), endedAt).getSeconds();
            if (pauseSecLong < 0) throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);

            open.resume(endedAt, (int) pauseSecLong);

            session.end(endedAt, session.getTotalFocusSec());

        } else {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        int finalFocusSec = session.getTotalFocusSec();

        session.markCountedInStats(finalFocusSec >= 300);

        return new FocusSessionEndResponse(
                session.getId(),
                session.getSessionStatus().name(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getTotalFocusSec(),
                session.getGoalDurationSec(),
                req.isRecorded(),
                session.isCountedInStats()
        );
    }
}
