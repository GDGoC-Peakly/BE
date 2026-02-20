package com.example.peakly.domain.home.service;

import com.example.peakly.domain.home.dto.response.DailyPeakTimeResponse;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePrediction;
import com.example.peakly.domain.peakTimePrediction.entity.PeakTimePredictionWindow;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionRepository;
import com.example.peakly.domain.peakTimePrediction.repository.PeakTimePredictionWindowRepository;
import com.example.peakly.global.apiPayload.code.status.HomeErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyPeakTimeServiceImpl implements DailyPeakTimeService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final PeakTimePredictionRepository peakTimePredictionRepository;
    private final PeakTimePredictionWindowRepository peakTimePredictionWindowRepository;

    @Override
    @Transactional(readOnly = true)
    public DailyPeakTimeResponse getDailyPeakTime(Long userId, String baseDateRaw) {

        LocalDate baseDate = parseBaseDateOrToday(baseDateRaw);

        var predictionOpt = peakTimePredictionRepository.findByUser_IdAndBaseDate(userId, baseDate);

        if (predictionOpt.isEmpty()) {
            return new DailyPeakTimeResponse(
                    baseDate.toString(),
                    List.of(),
                    null,
                    null
            );
        }

        PeakTimePrediction prediction = predictionOpt.get();

        List<PeakTimePredictionWindow> windows =
                peakTimePredictionWindowRepository.findAllByPrediction_IdOrderByStartMinuteOfDayAsc(prediction.getId());

        List<DailyPeakTimeResponse.WindowDTO> windowDtos = new ArrayList<>(windows.size());

        for (PeakTimePredictionWindow w : windows) {
            LocalDateTime startAt = baseDate.atStartOfDay().plusMinutes(w.getStartMinuteOfDay());
            LocalDateTime endAt = startAt.plusMinutes(w.getDurationMinutes());

            windowDtos.add(new DailyPeakTimeResponse.WindowDTO(
                    w.getRank(),
                    startAt,
                    endAt,
                    w.getScoreRaw(),
                    w.getScore01()
            ));
        }

        return new DailyPeakTimeResponse(
                baseDate.toString(),
                windowDtos,
                prediction.getModelVersion(),
                prediction.getComputedAt()
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
}