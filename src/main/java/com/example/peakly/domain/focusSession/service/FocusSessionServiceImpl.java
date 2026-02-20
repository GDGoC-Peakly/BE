package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.category.entity.Category;
import com.example.peakly.domain.category.entity.MajorCategory;
import com.example.peakly.domain.category.repository.CategoryRepository;
import com.example.peakly.domain.category.repository.MajorCategoryRepository;
import com.example.peakly.domain.dailySleep.repository.DailySleepLogRepository;
import com.example.peakly.domain.focusSession.command.FocusSessionStartCommand;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.*;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.focusSession.repository.SessionPauseRepository;
import com.example.peakly.domain.report.service.daily.DailyReportUpdateService;
import com.example.peakly.domain.report.util.ReportingDateUtil;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.CategoryErrorStatus;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {

    private static final int COUNTED_THRESHOLD_SEC = 300;
    private static final int CLIENT_SERVER_MISMATCH_WARN_SEC = 600;

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final MajorCategoryRepository majorCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SessionPauseRepository sessionPauseRepository;
    private final DailyReportUpdateService dailyReportUpdateService;
    private final DailySleepLogRepository dailySleepLogRepository;

    @Transactional
    public FocusSessionStartResponse start(Long userId, FocusSessionStartRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        var activeStatuses = List.of(SessionStatus.RUNNING, SessionStatus.PAUSED);

        List<FocusSession> activeSessions = focusSessionRepository.findActiveSessions(
                userId,
                activeStatuses,
                PageRequest.of(0, 1)
        );

        if (!activeSessions.isEmpty()) {
            FocusSession active = activeSessions.get(0);
            throw new GeneralException(
                    FocusSessionErrorStatus.SESSION_ALREADY_RUNNING,
                    new FocusSessionConflictPayload(active.getId())
            );
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

        boolean hasCheckin = dailySleepLogRepository.existsByUser_IdAndBaseDate(userId, baseDate);
        if (!hasCheckin) {
            throw new GeneralException(FocusSessionErrorStatus.DAILY_CHECKIN_REQUIRED);
        }

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
        accumulateRunningFocusSec(session, sessionId, pausedAt);

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

        int pauseSec = calcDeltaSec(open.getPausedAt(), resumedAt);

        open.resume(resumedAt, pauseSec);
        sessionPauseRepository.save(open);
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

        SessionStatus statusBeforeEnd = session.getSessionStatus();

        LocalDateTime endedAt = LocalDateTime.now();

        session.markRecorded(req.isRecorded());

        if (session.getSessionStatus() == SessionStatus.RUNNING) {
            accumulateRunningFocusSec(session, sessionId, endedAt);
            session.end(endedAt, COUNTED_THRESHOLD_SEC);

        } else if (session.getSessionStatus() == SessionStatus.PAUSED) {

            List<SessionPause> openPauses =
                    sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId);

            if (openPauses.size() != 1) {
                throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
            }

            SessionPause open = openPauses.get(0);

            int pauseSec = calcDeltaSec(open.getPausedAt(), endedAt);
            open.resume(endedAt, pauseSec);
            sessionPauseRepository.save(open);
            session.end(endedAt, COUNTED_THRESHOLD_SEC);

        } else {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        Integer clientSec = req.clientTotalFocusTimeSec();
        if (clientSec != null) {
            int serverSec = session.getTotalFocusSec();
            int diff = Math.abs(serverSec - clientSec);

            if (diff >= CLIENT_SERVER_MISMATCH_WARN_SEC) {
                log.warn(
                        "FocusSession end sanity check 결과가 일치하지 않습니다." +
                                "sessionId={}, userId={}, clientSec={}, serverSec={}, diffSec={}, statusBeforeEnd={}",
                        sessionId, userId, clientSec, serverSec, diff, statusBeforeEnd
                );
            }
        }

        try {
            LocalDate reportDate = ReportingDateUtil.reportingDateOf(endedAt);
            dailyReportUpdateService.updateReport(session.getUser(), reportDate);
        } catch (Exception e) {
            log.error("일간 리포트 업데이트 실패 (sessionId={}, userId={}). 세션 종료는 정상 처리됩니다.",
                    sessionId, userId, e);
        }

        return new FocusSessionEndResponse(
                session.getId(),
                session.getSessionStatus().name(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getTotalFocusSec(),
                session.getGoalDurationSec(),
                session.isRecorded(),
                session.isCountedInStats()
        );

    }

    private LocalDateTime resolveLastRunningStartedAt(Long sessionId, LocalDateTime sessionStartedAt) {
        return sessionPauseRepository
                .findTopByFocusSession_IdAndResumedAtIsNotNullOrderByResumedAtDesc(sessionId)
                .map(SessionPause::getResumedAt)
                .orElse(sessionStartedAt);
    }

    private int calcDeltaSec(LocalDateTime from, LocalDateTime to) {
        long sec = Duration.between(from, to).getSeconds();
        if (sec < 0) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }
        try {
            return Math.toIntExact(sec);
        } catch (ArithmeticException e) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }
    }

    private void accumulateRunningFocusSec(FocusSession session, Long sessionId, LocalDateTime now) {
        if (sessionPauseRepository.existsByFocusSession_IdAndResumedAtIsNull(sessionId)) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }

        LocalDateTime lastRunningStartedAt = resolveLastRunningStartedAt(sessionId, session.getStartedAt());
        int deltaSec = calcDeltaSec(lastRunningStartedAt, now);

        session.addFocusSec(deltaSec);
    }
}
