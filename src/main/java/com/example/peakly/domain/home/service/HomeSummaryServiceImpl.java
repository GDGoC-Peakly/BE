package com.example.peakly.domain.home.service;

import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.dailySleep.repository.DailySleepLogRepository;
import com.example.peakly.domain.focusSession.entity.SessionStatus;
import com.example.peakly.domain.focusSession.repository.FocusSessionRepository;
import com.example.peakly.domain.home.dto.response.HomeSummaryResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionWindowRepository;
import com.example.peakly.global.apiPayload.code.status.HomeErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomeSummaryServiceImpl implements HomeSummaryService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final DailySleepLogRepository dailySleepLogRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final PeakTimePredictionRepository peakTimePredictionRepository;
    private final PeakTimePredictionWindowRepository peakTimePredictionWindowRepository;

    @Override
    @Transactional(readOnly = true)
    public HomeSummaryResponse getHome(Long userId, String baseDateRaw) {

        LocalDate baseDate = parseBaseDateOrToday(baseDateRaw);

        LocalDateTime now = LocalDateTime.now(ZONE);

        DailySleepLog sleepLog = dailySleepLogRepository.findByUser_IdAndBaseDate(userId, baseDate)
                .orElseThrow(() -> new GeneralException(HomeErrorStatus.SLEEP_LOG_MISSING));

        long sleepSec = computeSleepSec(sleepLog);
        double sleepScore = sleepLog.getSleepScore().doubleValue();

        long totalFocusSec = Optional.ofNullable(
                focusSessionRepository.sumTotalFocusSecByUserIdAndBaseDateAndStatus(
                        userId, baseDate, SessionStatus.ENDED
                )
        ).orElse(0L);

        HomeSummaryResponse.PeakTimeDTO peaktime = buildPeakTimeDto(userId, baseDate, now);

        return new HomeSummaryResponse(
                baseDate.toString(),
                now,
                peaktime,
                new HomeSummaryResponse.SummaryDTO(
                        (int) totalFocusSec,
                        new HomeSummaryResponse.SleepDTO(
                                (int) sleepSec,
                                sleepScore
                        )
                )
        );
    }

    private LocalDate parseBaseDateOrToday(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDate.now(ZONE);
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new GeneralException(HomeErrorStatus.INVALID_BASE_DATE);
        }
    }

    private long computeSleepSec(DailySleepLog log) {
        LocalTime bed = log.getBedTime();
        LocalTime wake = log.getWakeTime();
        if (bed == null || wake == null) {
            throw new GeneralException(HomeErrorStatus.SLEEP_LOG_MISSING);
        }

        int bedSec = bed.toSecondOfDay();
        int wakeSec = wake.toSecondOfDay();

        int diff;
        if (wakeSec >= bedSec) diff = wakeSec - bedSec;
        else diff = (24 * 3600 - bedSec) + wakeSec;

        return diff;
    }

    private HomeSummaryResponse.PeakTimeDTO buildPeakTimeDto(Long userId, LocalDate baseDate, LocalDateTime now) {

        var predictionOpt = peakTimePredictionRepository.findByUser_IdAndBaseDate(userId, baseDate);

        if (predictionOpt.isEmpty()) {
            // 예측 결과 없음: windows=[], modelVersion/computedAt=null
            return new HomeSummaryResponse.PeakTimeDTO(
                    List.of(),
                    false,
                    null,
                    null,
                    null
            );
        }

        PeakTimePrediction prediction = predictionOpt.get();

        List<PeakTimePredictionWindow> windows =
                peakTimePredictionWindowRepository.findAllByPrediction_IdOrderByStartMinuteOfDayAsc(prediction.getId());

        List<HomeSummaryResponse.PeakWindowDTO> windowDtos = new ArrayList<>(windows.size());

        HomeSummaryResponse.PeakWindowDTO current = null;
        boolean isNowInPeakTime = false;

        for (PeakTimePredictionWindow w : windows) {
            LocalDateTime startedAt = baseDate.atStartOfDay().plusMinutes(w.getStartMinuteOfDay());
            LocalDateTime endedAt = startedAt.plusMinutes(w.getDurationMinutes());

            var dto = new HomeSummaryResponse.PeakWindowDTO(
                    w.getRank(),
                    startedAt,
                    endedAt,
                    w.getScoreRaw(),
                    w.getScore01()
            );
            windowDtos.add(dto);

            if (!isNowInPeakTime) {
                if ((now.isEqual(startedAt) || now.isAfter(startedAt)) && now.isBefore(endedAt)) {
                    isNowInPeakTime = true;
                    current = dto;
                }
            }
        }

        return new HomeSummaryResponse.PeakTimeDTO(
                windowDtos,
                isNowInPeakTime,
                current,
                prediction.getModelVersion(),
                prediction.getComputedAt()
        );
    }
}