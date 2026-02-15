package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.category.entity.Category;
import com.example.peakly.domain.category.entity.MajorCategory;
import com.example.peakly.domain.category.repository.CategoryRepository;
import com.example.peakly.domain.category.repository.MajorCategoryRepository;
import com.example.peakly.domain.focusSession.command.FocusSessionStartCommand;
import com.example.peakly.domain.focusSession.dto.request.FocusSessionStartRequest;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionStartResponse;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.CategoryErrorStatus;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
