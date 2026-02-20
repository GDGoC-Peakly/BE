package com.example.peakly.domain.peakTimePrediction.service;

import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictRequest;
import com.example.peakly.domain.peakTimePrediction.dto.ai.PeakTimePredictResponse;
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
            PeakTimePrediction prediction = predictionRepository
                    .findByUser_IdAndBaseDate(userId, baseDate)
                    .orElseGet(() -> PeakTimePrediction.create(user, baseDate, /*modelVersion*/ "ai", LocalDateTime.now()));

            prediction.updateComputedAt(LocalDateTime.now());
            prediction.updateModelVersion("ai");

            PeakTimePrediction saved = predictionRepository.save(prediction);


            windowRepository.deleteAllByPrediction_Id(saved.getId());

            double maxRaw = resp.top_peak_times().stream()
                    .filter(x -> x != null && x.score() != null)
                    .mapToDouble(x -> x.score())
                    .max()
                    .orElse(1.0);

            List<PeakTimePredictionWindow> windows = new java.util.ArrayList<>();
            int rank = 1;
            for (var x : resp.top_peak_times()) {
                if (x == null || x.hour() == null || x.duration() == null || x.score() == null) continue;

                windows.add(PeakTimePredictionWindow.fromAi(
                        saved,
                        rank++,
                        x.hour(),
                        x.duration(),
                        x.score(),
                        maxRaw
                ));
            }

            windows.sort(Comparator.comparingInt(PeakTimePredictionWindow::getStartMinuteOfDay));
            windowRepository.saveAll(windows);

            return saved;

        } catch (DataIntegrityViolationException e) {
            return predictionRepository.findByUser_IdAndBaseDate(userId, baseDate)
                    .orElseThrow(() -> new GeneralException(PeakTimePredictionErrorStatus.PREDICTION_UPSERT_FAILED));
        }
    }
}