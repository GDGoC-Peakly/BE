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
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    @Transactional
    public InitialSettingResponse saveInitialSetting(
            Long userId,
            @Valid @NotNull InitialSettingRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorStatus.USER_404_001));

        if (initialDataRepository.existsByUserId(userId)) {
            throw new GeneralException(UserErrorStatus.USER_409_001);
        }

        user.updateJob(req.job());

        InitialDataCreateCommand cmd = new InitialDataCreateCommand(
                req.chronotype(),
                req.subjectivePeaktime(),
                req.caffeineResponsiveness(),
                req.noiseSensitivity()
        );

        InitialData data = InitialData.create(user, cmd);

        InitialData saved;
        try {
            saved = initialDataRepository.save(data);
            initialDataRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(UserErrorStatus.USER_409_001);
        }

        return new InitialSettingResponse(
                userId,
                user.getJob(),
                saved.getChronotype(),
                saved.getSubjectivePeaktime(),
                saved.getCaffeineResponsiveness(),
                saved.getNoiseSensitivity(),
                saved.getCreatedAt().atZone(DEFAULT_ZONE).toOffsetDateTime()
        );
    }
}
