package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.command.InitialDataCreateCommand;
import com.example.peakly.domain.user.dto.request.InitialSettingRequest;
import com.example.peakly.domain.user.dto.response.InitialSettingResponse;
import com.example.peakly.domain.user.entity.InitialData;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.InitialDataRepository;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class InitialDataServiceImpl implements InitialDataService {

    private final UserRepository userRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    @Transactional
    public InitialSettingResponse createInitialSetting(Long userId, InitialSettingRequest req) {
        if (userId == null) {
            throw new GeneralException(UserErrorStatus.USER_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        if (initialDataRepository.existsByUserId(userId)) {
            throw new GeneralException(UserErrorStatus.INITIAL_DATA_ALREADY_REGISTERED);
        }

        user.updateJob(req.job());

        InitialDataCreateCommand cmd = new InitialDataCreateCommand(
                req.chronotype(),
                req.subjectivePeaktime(),
                req.caffeineResponsiveness(),
                req.noiseSensitivity()
        );

        InitialData entity = InitialData.create(user, cmd);

        try {
            InitialData saved = initialDataRepository.save(entity);

            OffsetDateTime recordedAt = resolveRecordedAt(saved);

            return new InitialSettingResponse(
                    saved.getUserId(),
                    user.getJob(),
                    saved.getChronotype(),
                    saved.getSubjectivePeaktime(),
                    saved.getCaffeineResponsiveness(),
                    saved.getNoiseSensitivity(),
                    recordedAt
            );
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(UserErrorStatus.INITIAL_DATA_ALREADY_REGISTERED);
        }
    }

    private OffsetDateTime resolveRecordedAt(InitialData saved) {
        return saved.getCreatedAt()
                .atOffset(ZoneOffset.ofHours(9)); // Asia/Seoul 기준
    }
}
