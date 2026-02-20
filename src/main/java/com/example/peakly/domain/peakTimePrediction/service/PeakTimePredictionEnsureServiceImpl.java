package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;
import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictResponse;
import com.example.peakly.domain.peakTimePrediction.dto.response.PeakTimePredictionRefreshResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import com.example.peakly.domain.peakTimePrediction.infra.PeakTimeAiClient;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionWindowRepository;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.PeakTimePredictionErrorStatus;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeakTimePredictionEnsureServiceImpl implements PeakTimePredictionEnsureService {

    private final PeakTimePredictionRepository predictionRepository;
    private final PeakTimePredictionWindowRepository windowRepository;
    private final UserRepository userRepository;

    private final PeakTimePredictRequestProvider requestProvider;
    private final PeakTimeAiClient aiClient;

    private final PeakTimePredictionFallbackReader fallbackReader;

    @Override
    @Transactional
    public PeakTimePrediction ensure(Long userId, LocalDate baseDate) {
        return predictionRepository.findByUser_IdAndBaseDate(userId, baseDate)
                .orElseGet(() -> refresh(userId, baseDate));
    }

    @Override
    @Transactional
    public PeakTimePrediction refresh(Long userId, LocalDate baseDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        PeakTimePredictRequest req = requestProvider.build(userId, baseDate);

        PeakTimePredictResponse resp = aiClient.predict(req);
        if (resp == null || resp.top_peak_times() == null) {
            throw new GeneralException(PeakTimePredictionErrorStatus.AI_BAD_RESPONSE);
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            PeakTimePrediction prediction = predictionRepository
                    .findByUser_IdAndBaseDate(userId, baseDate)
                    .orElseGet(() -> PeakTimePrediction.create(user, baseDate, "v1.0.0", now));

            prediction.updateComputedAt(now);
            prediction.updateModelVersion("v1.0.0");

            double maxRaw = resp.top_peak_times().stream()
                    .filter(x -> x != null && x.score() != null)
                    .mapToDouble(x -> x.score())
                    .max()
                    .orElse(1.0);

            List<PeakTimePredictionWindow> newWindows = new ArrayList<>();
            int rank = 1;

            for (var x : resp.top_peak_times()) {
                if (x == null || x.hour() == null || x.duration() == null || x.score() == null) continue;

                int startMinute = toMinuteOfDayRounded(x.hour());
                int durationMin = toMinutesRounded(x.duration());
                if (durationMin <= 0) continue;

                newWindows.add(PeakTimePredictionWindow.of(
                        prediction,
                        rank++,
                        startMinute,
                        durationMin,
                        x.score(),
                        maxRaw
                ));
            }

            newWindows.sort(Comparator.comparingInt(PeakTimePredictionWindow::getStartMinuteOfDay));

            newWindows = dropOverlapsKeepEarlier(newWindows);

            prediction.replaceWindows(newWindows);

            return predictionRepository.save(prediction);

        } catch (DataIntegrityViolationException e) {
            return fallbackReader.findOrFail(userId, baseDate);
        }
    }

    @Override
    @Transactional
    public PeakTimePredictionRefreshResponse refreshResponse(Long userId, LocalDate baseDate) {
        PeakTimePrediction saved = refresh(userId, baseDate);

        List<PeakTimePredictionWindow> windowEntities =
                windowRepository.findAllByPrediction_IdOrderByStartMinuteOfDayAsc(saved.getId());

        List<PeakTimePredictionRefreshResponse.WindowDTO> windows = windowEntities.stream()
                .map(w -> {
                    LocalDateTime startAt = baseDate.atStartOfDay().plusMinutes(w.getStartMinuteOfDay());
                    LocalDateTime endAt = startAt.plusMinutes(w.getDurationMinutes());
                    return new PeakTimePredictionRefreshResponse.WindowDTO(
                            w.getRank(),
                            startAt,
                            endAt,
                            w.getScoreRaw(),
                            w.getScore01()
                    );
                })
                .toList();

        return new PeakTimePredictionRefreshResponse(
                saved.getId(),
                saved.getBaseDate(),
                saved.getModelVersion(),
                saved.getComputedAt(),
                windows
        );
    }

    private int toMinuteOfDayRounded(Double hour) {
        int m = (int) Math.round(hour * 60.0);
        if (m < 0) m = 0;
        if (m >= 24 * 60) m = 24 * 60 - 1;
        return m;
    }

    private int toMinutesRounded(Double durationHours) {
        return (int) Math.round(durationHours * 60.0);
    }

    // 겹치면 뒤(나중) 윈도우를 버리고, 먼저 나온 윈도우를 유지
    private List<PeakTimePredictionWindow> dropOverlapsKeepEarlier(List<PeakTimePredictionWindow> sorted) {
        List<PeakTimePredictionWindow> out = new ArrayList<>();
        int lastEnd = -1;

        for (PeakTimePredictionWindow w : sorted) {
            int s = w.getStartMinuteOfDay();
            int e = s + w.getDurationMinutes();
            if (s < lastEnd) {
                // 겹치면 버림
                continue;
            }
            out.add(w);
            lastEnd = e;
        }
        return out;
    }
}