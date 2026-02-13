package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.command.InitialDataCreateCommand;
import com.example.peakly.domain.user.dto.request.InitialDataCreateRequest;
import com.example.peakly.domain.user.dto.response.InitialDataCreateResponse;
import com.example.peakly.domain.user.entity.InitialData;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.InitialDataRepository;
import com.example.peakly.domain.user.repository.UserRepository;
import com.example.peakly.global.apiPayload.code.status.UserErrorStatus;
import com.example.peakly.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitialDataServiceImpl implements InitialDataService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    @Transactional
    public InitialDataCreateResponse createInitialData(Long userId, InitialDataCreateRequest req) {
        if (userId == null) {
            throw new GeneralException(UserErrorStatus.USER_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_NOT_FOUND));

        if (initialDataRepository.existsByUserId(userId)) {
            throw new GeneralException(UserErrorStatus.INITIAL_DATA_ALREADY_REGISTERED);
        }

        InitialDataCreateCommand cmd = new InitialDataCreateCommand(
                req.chronotype(),
                req.subjectivePeaktime(),
                req.caffeineResponsiveness(),
                req.noiseSensitivity()
        );

        InitialData entity = InitialData.create(user, cmd);

        try {
            user.updateJob(req.job());

            InitialData saved = initialDataRepository.save(entity);

            OffsetDateTime recordedAt = resolveRecordedAt(saved);

            return new InitialDataCreateResponse(
                    saved.getUserId(),
                    user.getJob(),
                    saved.getChronotype(),
                    saved.getSubjectivePeaktime(),
                    saved.getCaffeineResponsiveness(),
                    saved.getNoiseSensitivity(),
                    recordedAt
            );
        } catch (DataIntegrityViolationException e) {
            log.warn("초기 데이터 중복 등록 시도: userId={}", userId, e);
            throw new GeneralException(UserErrorStatus.INITIAL_DATA_ALREADY_REGISTERED);
        }
    }

    private OffsetDateTime resolveRecordedAt(InitialData saved) {
        if (saved.getCreatedAt() == null) {
            return OffsetDateTime.now(KST);
        }
        return saved.getCreatedAt()
                .atZone(KST)
                .toOffsetDateTime();
    }
}
