package com.example.peakly.domain.report;

import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.report.util.FocusSessionSlotCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class FocusSessionSlotCalculatorTest {

    private FocusSessionSlotCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FocusSessionSlotCalculator();
    }

    @Test
    @DisplayName("슬롯과 세션이 완전히 겹치면 30분 반환")
    void fullOverlap_returns30() {
        FocusSession session = mockSession(
                LocalDateTime.of(2026, 2, 17, 10, 0),
                LocalDateTime.of(2026, 2, 17, 10, 30)
        );

        int result = calculator.calcActualMinInSlot(List.of(session), LocalTime.of(10, 0));
        assertThat(result).isEqualTo(30);
    }

    @Test
    @DisplayName("슬롯과 세션이 일부만 겹치면 겹친 시간만 반환")
    void partialOverlap_returns15() {
        FocusSession session = mockSession(
                LocalDateTime.of(2026, 2, 17, 10, 15),
                LocalDateTime.of(2026, 2, 17, 11, 0)
        );

        int result = calculator.calcActualMinInSlot(List.of(session), LocalTime.of(10, 0));
        assertThat(result).isEqualTo(15);
    }

    @Test
    @DisplayName("슬롯과 세션이 겹치지 않으면 0 반환")
    void noOverlap_returns0() {
        FocusSession session = mockSession(
                LocalDateTime.of(2026, 2, 17, 11, 0),
                LocalDateTime.of(2026, 2, 17, 12, 0)
        );

        int result = calculator.calcActualMinInSlot(List.of(session), LocalTime.of(10, 0));
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("세션 여러 개일 때 겹친 시간 합산")
    void multipleSessions_sumTo30() {
        FocusSession session1 = mockSession(
                LocalDateTime.of(2026, 2, 17, 10, 0),
                LocalDateTime.of(2026, 2, 17, 10, 15)
        );
        FocusSession session2 = mockSession(
                LocalDateTime.of(2026, 2, 17, 10, 15),
                LocalDateTime.of(2026, 2, 17, 10, 30)
        );

        int result = calculator.calcActualMinInSlot(List.of(session1, session2), LocalTime.of(10, 0));
        assertThat(result).isEqualTo(30);
    }

    private FocusSession mockSession(LocalDateTime startedAt, LocalDateTime endedAt) {
        FocusSession session = mock(FocusSession.class);
        given(session.getStartedAt()).willReturn(startedAt);
        given(session.getEndedAt()).willReturn(endedAt);
        return session;
    }
}