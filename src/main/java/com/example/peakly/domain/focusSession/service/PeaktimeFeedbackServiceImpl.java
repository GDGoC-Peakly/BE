package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.command.PeaktimeFeedbackCreateCommand;
import com.example.peakly.domain.focusSession.dto.request.PeaktimeFeedbackCreateRequest;
import com.example.peakly.domain.focusSession.dto.request.SessionDisruptionsSaveRequest;
import com.example.peakly.domain.focusSession.dto.response.PeaktimeFeedbackCreateResponse;
import com.example.peakly.domain.focusSession.dto.response.SessionDisruptionsSaveResponse;
import com.example.peakly.domain.focusSession.dto.result.PeaktimeFeedbackDTO;
import com.example.peakly.domain.focusSession.entity.DisruptionReason;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.PeaktimeFeedback;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.DisruptionReasonRepository;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.focusSession.repository.PeaktimeFeedbackRepository;
import com.example.peakly.global.apiPayload.code.status.FeedbackErrorStatus;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PeaktimeFeedbackServiceImpl implements PeaktimeFeedbackService {

    private final FocusSessionRepository focusSessionRepository;
    private final PeaktimeFeedbackRepository peaktimeFeedbackRepository;
    private final DisruptionReasonRepository disruptionReasonRepository;

    @Override
    @Transactional
    public PeaktimeFeedbackCreateResponse createFeedback(Long userId, Long sessionId, PeaktimeFeedbackCreateRequest req) {
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.ENDED) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_NOT_ENDED);
        }

        if (peaktimeFeedbackRepository.existsByFocusSession_Id(sessionId)) {
            throw new GeneralException(FocusSessionErrorStatus.FEEDBACK_ALREADY_EXISTS);
        }

        int score = req.focusScore();
        PeaktimeFeedbackCreateCommand cmd = new PeaktimeFeedbackCreateCommand(score);

        PeaktimeFeedback feedback = PeaktimeFeedback.create(session, cmd);
        PeaktimeFeedback saved = peaktimeFeedbackRepository.save(feedback);

        PeaktimeFeedbackDTO dto = new PeaktimeFeedbackDTO(
                sessionId,
                saved.getFocusScore(),
                saved.getCreatedAt()
        );

        return new PeaktimeFeedbackCreateResponse(dto);
    }

    @Override
    @Transactional
    public SessionDisruptionsSaveResponse saveDisruptions(Long userId, Long sessionId, SessionDisruptionsSaveRequest req) {
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.ENDED) {
            throw new GeneralException(FocusSessionErrorStatus.INVALID_SESSION_STATE);
        }

        PeaktimeFeedback feedback = peaktimeFeedbackRepository.findByFocusSessionIdForUpdate(sessionId)
                .orElseThrow(() -> new GeneralException(FeedbackErrorStatus.FEEDBACK_NOT_FOUND));

        if (feedback.getFocusScore() > 2) {
            throw new GeneralException(FeedbackErrorStatus.DISRUPTION_NOT_ALLOWED_FOR_HIGH_SCORE);
        }

        List<Long> ids = req.disruptionReasonIds();
        if (ids == null || ids.isEmpty()) {
            throw new GeneralException(FeedbackErrorStatus.DISRUPTION_REQUIRED);
        }

        Set<Long> unique = new LinkedHashSet<>(ids);
        if (unique.size() != ids.size()) {
            throw new GeneralException(FeedbackErrorStatus.INVALID_DISRUPTION_REASON_ID);
        }

        if (!feedback.getDisruptionReasons().isEmpty()) {
            throw new GeneralException(FeedbackErrorStatus.DISRUPTION_ALREADY_EXISTS);
        }

        List<Long> uniqueIds = new ArrayList<>(unique);
        List<DisruptionReason> reasons = disruptionReasonRepository.findAllByIdIn(uniqueIds);

        if (reasons.size() != uniqueIds.size()) {
            throw new GeneralException(FeedbackErrorStatus.INVALID_DISRUPTION_REASON_ID);
        }
        for (DisruptionReason r : reasons) {
            if (!r.isActive()) {
                throw new GeneralException(FeedbackErrorStatus.INACTIVE_DISRUPTION_REASON);
            }
        }

        for (DisruptionReason r : reasons) {
            feedback.addDisruptionReason(r);
        }

        LocalDateTime recordedAt = LocalDateTime.now();

        return new SessionDisruptionsSaveResponse(sessionId, uniqueIds, recordedAt);
    }
}
