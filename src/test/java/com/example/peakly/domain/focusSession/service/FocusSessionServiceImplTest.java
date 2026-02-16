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
    void end_running_accumulatesFocus_andEnds_andSetsCountedInStats_andMarksRecorded() {
        FocusSession session = mock(FocusSession.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        java.util.concurrent.atomic.AtomicReference<SessionStatus> statusRef =
                new java.util.concurrent.atomic.AtomicReference<>(SessionStatus.RUNNING);

        when(session.getSessionStatus()).thenAnswer(inv -> statusRef.get());

        doAnswer(inv -> {
            statusRef.set(SessionStatus.ENDED);
            return null;
        }).when(session).end(any(LocalDateTime.class), eq(300));

        when(session.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(10));

        when(sessionPauseRepository.existsByFocusSession_IdAndResumedAtIsNull(sessionId))
                .thenReturn(false);

        when(sessionPauseRepository.findLatestResumedPause(sessionId))
                .thenReturn(Optional.empty());

        when(session.getId()).thenReturn(sessionId);
        when(session.getEndedAt()).thenReturn(LocalDateTime.now());
        when(session.getTotalFocusSec()).thenReturn(600);
        when(session.getGoalDurationSec()).thenReturn(1200);
        when(session.isCountedInStats()).thenReturn(true);

        java.util.concurrent.atomic.AtomicBoolean recordedRef = new java.util.concurrent.atomic.AtomicBoolean(false);
        when(session.isRecorded()).thenAnswer(inv -> recordedRef.get());
        doAnswer(inv -> {
            recordedRef.set(inv.getArgument(0, Boolean.class));
            return null;
        }).when(session).markRecorded(anyBoolean());

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);
        when(req.isRecorded()).thenReturn(true);
        when(req.clientTotalFocusTimeSec()).thenReturn(590);

        FocusSessionEndResponse res = service.end(userId, sessionId, req);

        verify(session, atLeastOnce()).addFocusSec(anyInt());
        verify(session, times(1)).end(any(LocalDateTime.class), eq(300));

        ArgumentCaptor<Boolean> recordedCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(session, times(1)).markRecorded(recordedCaptor.capture());
        assertTrue(recordedCaptor.getValue());

        assertEquals(sessionId, res.sessionId());
        assertEquals("ENDED", res.sessionStatus());
        assertTrue(res.isRecorded());
        assertTrue(res.isCountedInStats());
    }


    @Test
    void end_paused_resumesOpenPause_andEnds_andMarksRecorded() {
        FocusSession session = mock(FocusSession.class);
        SessionPause open = mock(SessionPause.class);

        when(focusSessionRepository.findByIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(session));

        java.util.concurrent.atomic.AtomicReference<SessionStatus> statusRef =
                new java.util.concurrent.atomic.AtomicReference<>(SessionStatus.PAUSED);

        when(session.getSessionStatus()).thenAnswer(inv -> statusRef.get());

        doAnswer(inv -> {
            statusRef.set(SessionStatus.ENDED);
            return null;
        }).when(session).end(any(LocalDateTime.class), eq(300));

        when(sessionPauseRepository.findAllByFocusSession_IdAndResumedAtIsNull(sessionId))
                .thenReturn(List.of(open));

        when(open.getPausedAt()).thenReturn(LocalDateTime.now().minusMinutes(3));

        when(session.getId()).thenReturn(sessionId);
        when(session.getStartedAt()).thenReturn(LocalDateTime.now().minusMinutes(30));
        when(session.getEndedAt()).thenReturn(LocalDateTime.now());
        when(session.getTotalFocusSec()).thenReturn(1000);
        when(session.getGoalDurationSec()).thenReturn(1200);
        when(session.isCountedInStats()).thenReturn(true);

        java.util.concurrent.atomic.AtomicBoolean recordedRef = new java.util.concurrent.atomic.AtomicBoolean(true);
        when(session.isRecorded()).thenAnswer(inv -> recordedRef.get());
        doAnswer(inv -> {
            recordedRef.set(inv.getArgument(0, Boolean.class));
            return null;
        }).when(session).markRecorded(anyBoolean());

        FocusSessionEndRequest req = mock(FocusSessionEndRequest.class);
        when(req.isRecorded()).thenReturn(false);
        when(req.clientTotalFocusTimeSec()).thenReturn(0);

        FocusSessionEndResponse res = service.end(userId, sessionId, req);

        verify(open, times(1)).resume(any(LocalDateTime.class), anyInt());
        verify(session, times(1)).end(any(LocalDateTime.class), eq(300));

        ArgumentCaptor<Boolean> recordedCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(session, times(1)).markRecorded(recordedCaptor.capture());
        assertFalse(recordedCaptor.getValue());

        assertEquals(sessionId, res.sessionId());
        assertEquals("ENDED", res.sessionStatus());
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
