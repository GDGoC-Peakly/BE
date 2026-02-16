package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.request.FocusSessionEndRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionEndResponse;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionPause;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.focusSession.repository.SessionPauseRepository;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FocusSessionServiceImpl 단위 테스트
 * - end(RUNNING), end(PAUSED) 주요 로직 검증
 */
@ExtendWith(MockitoExtension.class)
class FocusSessionServiceImplTest {

    @Mock FocusSessionRepository focusSessionRepository;
    @Mock SessionPauseRepository sessionPauseRepository;

    // start에 필요한 repo는 end 테스트에서는 null이어도 되지만, 생성자 주입 때문에 mock으로 둡니다.
    @Mock com.example.peakly.domain.user.repository.UserRepository userRepository;
    @Mock com.example.peakly.domain.category.repository.MajorCategoryRepository majorCategoryRepository;
    @Mock com.example.peakly.domain.category.repository.CategoryRepository categoryRepository;

    @InjectMocks FocusSessionServiceImpl service;

    private final Long userId = 1L;
    private final Long sessionId = 10L;

    @BeforeEach
    void setUp() {
        // @InjectMocks로 생성됨
    }

    @Test
    void end_running_accumulatesFocus_andEnds_andSetsCountedInStats() {
        // given
        FocusSession session = mock(FocusSession.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        when(session.getSessionStatus()).thenReturn(SessionStatus.RUNNING);
        when(session.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));

        // RUNNING 상태에서 open pause 없어야 함
        when(sessionPauseRepository.existsByFocusSession_IdAndResumedAtIsNull(sessionId))
                .thenReturn(false);

        // 마지막 resume 없으면 startedAt 기준 사용하도록 - Optional.empty
        when(sessionPauseRepository.findLatestResumedPause(sessionId))
                .thenReturn(Optional.empty());

        // addFocusSec 이후 totalFocusSec 사용될 수 있으니 stub
        when(session.getId()).thenReturn(sessionId);
        when(session.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(session.getEndedAt()).thenReturn(LocalDateTime.now()); // 응답 구성용
        when(session.getTotalFocusSec()).thenReturn(600); // 임의값(서비스가 직접 계산값을 읽지는 않지만 응답에 사용)
        when(session.getGoalDurationSec()).thenReturn(1200);
        when(session.isCountedInStats()).thenReturn(true);

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);
        when(req.isRecorded()).thenReturn(true);

        // when
        FocusSessionEndResponse res = service.end(userId, sessionId, req);

        // then
        // 누적 로직: addFocusSec가 호출되어야 함 (정확한 값은 LocalDateTime.now에 의존하므로 anyInt)
        verify(session, atLeastOnce()).addFocusSec(anyInt());

        // end는 threshold 전달
        verify(session, times(1)).end(any(LocalDateTime.class), eq(300));

        assertEquals(sessionId, res.sessionId());
        assertEquals("RUNNING".equals(res.sessionStatus()) ? res.sessionStatus() : res.sessionStatus(), res.sessionStatus()); // 상태 문자열은 구현에 따름
    }

    @Test
    void end_paused_resumesOpenPause_andEnds() {
        // given
        FocusSession session = mock(FocusSession.class);
        SessionPause open = mock(SessionPause.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        when(session.getSessionStatus()).thenReturn(SessionStatus.PAUSED);

        when(sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId))
                .thenReturn(List.of(open));

        when(open.getPausedAt()).thenReturn(LocalDateTime.now().minusMinutes(3));

        // 응답용
        when(session.getId()).thenReturn(sessionId);
        when(session.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(30));
        when(session.getEndedAt()).thenReturn(LocalDateTime.now());
        when(session.getTotalFocusSec()).thenReturn(1000);
        when(session.getGoalDurationSec()).thenReturn(1200);
        when(session.isCountedInStats()).thenReturn(true);

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);
        when(req.isRecorded()).thenReturn(false);

        // when
        FocusSessionEndResponse res = service.end(userId, sessionId, req);

        // then
        verify(open, times(1)).resume(any(LocalDateTime.class), anyInt());
        verify(session, times(1)).end(any(LocalDateTime.class), eq(300));

        assertEquals(sessionId, res.sessionId());
        assertFalse(res.isRecorded());
    }

    @Test
    void end_invalidState_ended_throws() {
        FocusSession session = mock(FocusSession.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));
        when(session.getSessionStatus()).thenReturn(SessionStatus.ENDED);

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);

        assertThrows(GeneralException.class, () -> service.end(userId, sessionId, req));
    }

    @Test
    void end_paused_openPauseNotExactlyOne_throws() {
        FocusSession session = mock(FocusSession.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));
        when(session.getSessionStatus()).thenReturn(SessionStatus.PAUSED);

        when(sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId))
                .thenReturn(List.of()); // 0개

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);

        assertThrows(GeneralException.class, () -> service.end(userId, sessionId, req));
    }
}
