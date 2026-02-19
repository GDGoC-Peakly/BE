package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.category.entity.Category;
import com.example.peakly.domain.category.entity.MajorCategory;
import com.example.peakly.domain.category.repository.CategoryRepository;
import com.example.peakly.domain.category.repository.MajorCategoryRepository;
import com.example.peakly.domain.focusSession.dto.response.FocusSessionResultResponse;
import com.example.peakly.domain.focusSession.entity.FocusSession;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.global.apiPayload.code.status.CategoryErrorStatus;
import com.example.peakly.global.apiPayload.code.status.FocusSessionErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusSessionResultServiceImpl implements FocusSessionResultService {

    private final FocusSessionRepository focusSessionRepository;
    private final MajorCategoryRepository majorCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public FocusSessionResultResponse getResult(Long userId,  Long sessionId) {
        FocusSession session = focusSessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new GeneralException(FocusSessionErrorStatus.SESSION_NOT_FOUND));

        if (session.getSessionStatus() != SessionStatus.ENDED && session.getSessionStatus() != SessionStatus.CANCELED) {
            throw new GeneralException(FocusSessionErrorStatus.SESSION_NOT_ENDED);
        }

        int goalDurationSec = session.getGoalDurationSec();
        int totalFocusSec = session.getTotalFocusSec();

        double achievementRate = 0.0;
        if (goalDurationSec > 0) {
            double raw = Math.min(100.0, (totalFocusSec * 100.0) / goalDurationSec);
            achievementRate = Math.round(raw * 10.0) / 10.0;
        }

        Long majorCategoryId = session.getMajorCategoryId();
        MajorCategory major = majorCategoryRepository.findById(majorCategoryId)
                .orElseThrow(() -> new GeneralException(CategoryErrorStatus.MAJOR_CATEGORY_NOT_FOUND));

        FocusSessionResultResponse.CategoryRef majorRef =
                new FocusSessionResultResponse.CategoryRef(major.getId(), major.getName());

        FocusSessionResultResponse.CategoryRef categoryRef = null;
        Long categoryId = session.getCategoryId();
        if (categoryId != null) {
            Category category = categoryRepository.findByIdAndUser_Id(categoryId, userId)
                    .orElseThrow(() -> new GeneralException(CategoryErrorStatus.CATEGORY_NOT_FOUND));
            categoryRef = new FocusSessionResultResponse.CategoryRef(category.getId(), category.getName());
        }

        var env = new FocusSessionResultResponse.Environment(
                (int) session.getFatigueLevel(),
                (int) session.getCaffeineIntakeLevel(),
                (int) session.getNoiseLevel()
        );

        return new FocusSessionResultResponse(
                session.getId(),
                session.getSessionStatus().name(),
                majorRef,
                categoryRef,
                session.getStartedAt(),
                session.getEndedAt(),
                goalDurationSec,
                totalFocusSec,
                achievementRate,
                session.isCountedInStats(),
                env
        );
    }
}