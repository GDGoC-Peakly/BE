package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.peakTimePrediction.dto.response.SessionPeakTimeOverlapsResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionWindowRepository;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionPeakTimeOverlapsServiceImpl implements SessionPeakTimeOverlapsService {

    private final FocusSessionRepository focusSessionRepository;
    private final PeakTimePredictionRepository peakTimePredictionRepository;
    private final PeakTimePredictionWindowRepository peakTimePredictionWindowRepository;

    @Override
    public SessionPeakTimeOverlapsResponse getSessionPeakTimeOverlaps(Long userId, Long sessionId) {

        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (!session.getUser().getId().equals(userId)) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_FORBIDDEN);
        }

        if (session.getSessionStatus() != SessionStatus.ENDED) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_NOT_ENDED);
        }

        LocalDate baseDate = session.getBaseDate();

        PeakTimePrediction prediction = peakTimePredictionRepository
                .findByUser_IdAndBaseDate(userId, baseDate)
                .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PEAKTIME_PREDICTION_NOT_FOUND));

        List<PeakTimePredictionWindow> windows =
                peakTimePredictionWindowRepository.findAllByPrediction_IdOrderByStartMinuteOfDayAsc(prediction.getId());

        LocalDateTime sessionStart = session.getStartedAt();
        LocalDateTime sessionEnd = session.getEndedAt();
        if (sessionEnd == null) {
            throw new GeneralException(FocusSessionErrorStatus.DATA_INCONSISTENCY);
        }

        List<SessionPeakTimeOverlapsResponse.WindowDTO> matchedWindows = new ArrayList<>();
        List<SessionPeakTimeOverlapsResponse.OverlapDTO> overlaps = new ArrayList<>();

        for (PeakTimePredictionWindow w : windows) {
            LocalDateTime wStart = baseDate.atStartOfDay().plusMinutes(w.getStartMinuteOfDay());
            LocalDateTime wEnd = wStart.plusMinutes(w.getDurationMinutes());

            LocalDateTime overlapStart = sessionStart.isAfter(wStart) ? sessionStart : wStart;
            LocalDateTime overlapEnd = sessionEnd.isBefore(wEnd) ? sessionEnd : wEnd;

            if (overlapStart.isBefore(overlapEnd)) {
                matchedWindows.add(new SessionPeakTimeOverlapsResponse.WindowDTO(
                        w.getRank(),
                        wStart,
                        wEnd,
                        w.getScoreRaw(),
                        w.getScore01()
                ));

                overlaps.add(new SessionPeakTimeOverlapsResponse.OverlapDTO(
                        w.getRank(),
                        overlapStart,
                        overlapEnd
                ));
            }
        }

        var sessionDto = new SessionPeakTimeOverlapsResponse.SessionDTO(
                session.getId(),
                sessionStart,
                sessionEnd,
                session.getTotalFocusSec()
        );

        return new SessionPeakTimeOverlapsResponse(
                baseDate,
                sessionDto,
                matchedWindows,
                overlaps,
                prediction.getModelVersion(),
                prediction.getComputedAt()
        );
    }
}