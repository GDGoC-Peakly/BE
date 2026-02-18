package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.command.PeaktimeFeedbackCreateCommand;
import com.example.peakly.domain.focusSession.dto.request.PeaktimeFeedbackCreateRequest;
import com.example.peakly.domain.focusSession.dto.response.PeaktimeFeedbackCreateResponse;
import com.example.peakly.domain.focusSession.dto.result.PeaktimeFeedbackDTO;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.PeaktimeFeedback;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.focusSession.repository.PeaktimeFeedbackRepository;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeaktimeFeedbackServiceImpl implements PeaktimeFeedbackService {

    private final FocusSessionRepository focusSessionRepository;
    private final PeaktimeFeedbackRepository peaktimeFeedbackRepository;

    @Override
    @Transactional
    public PeaktimeFeedbackCreateResponse createFeedback(Long userId, Long sessionId, PeaktimeFeedbackCreateRequest req) {

        // 1) 세션 존재 + 소유자 검증 (repo 메서드가 이미 존재하니 그대로 활용)
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        // 2) 세션 상태 ENDED만 허용
        if (session.getSessionStatus() != SessionStatus.ENDED) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_NOT_ENDED);
        }

        // 3) 중복 피드백 방지 (unique + exists로 이중 안전)
        if (peaktimeFeedbackRepository.existsByFocusSession_Id(sessionId)) {
            throw new GeneralException(FocusSessionErrorStatus.FEEDBACK_ALREADY_EXISTS);
        }

        // 4) 생성
        int score = req.focusScore(); // @Valid가 걸려 있으면 null/범위는 이미 걸러짐
        PeaktimeFeedbackCreateCommand cmd = new PeaktimeFeedbackCreateCommand(score);

        PeaktimeFeedback feedback = PeaktimeFeedback.create(session, cmd);
        PeaktimeFeedback saved = peaktimeFeedbackRepository.save(feedback);

        // 5) 응답 recordedAt은 createdAt 사용 (AuditingEntityListener)
        PeaktimeFeedbackDTO dto = new PeaktimeFeedbackDTO(
                sessionId,
                saved.getFocusScore(),
                saved.getCreatedAt()
        );

        return new PeaktimeFeedbackCreateResponse(dto);
    }
}
