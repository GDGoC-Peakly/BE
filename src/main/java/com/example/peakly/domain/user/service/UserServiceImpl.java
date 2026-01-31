package com.example.peakly.domain.user.service;

import com.example.peakly.domain.user.command.InitialDataCreateCommand;
import com.example.peakly.domain.user.dto.request.InitialSettingRequest;
import com.example.peakly.domain.user.dto.response.InitialSettingResponse;
import com.example.peakly.domain.user.entity.InitialData;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.InitialDataRepository;
import com.example.peakly.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InitialDataRepository initialDataRepository;

    @Override
    @Transactional
    public InitialSettingResponse saveInitialSetting(Long userId, InitialSettingRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다.")); // 여기 GeneralException으로 바꾸셔도 됩니다.

        if (initialDataRepository.existsByUserId(userId)) {
            throw new IllegalStateException("초기 데이터가 이미 등록되어 있습니다.");
        }

        user.updateJob(req.job());

        InitialDataCreateCommand cmd = new InitialDataCreateCommand(
                req.chronotype(),
                req.subjectivePeaktime(),
                req.caffeineResponsiveness(),
                req.noiseSensitivity()
        );

        InitialData data = InitialData.create(user, cmd);
        InitialData saved = initialDataRepository.save(data);

        String recordedAt = saved.getCreatedAt().toString();

        return new InitialSettingResponse(
                userId,
                req.job(),
                req.chronotype(),
                req.subjectivePeaktime(),
                req.caffeineResponsiveness(),
                req.noiseSensitivity(),
                recordedAt
        );
    }
}
