package com.example.peakly.domain.focusSession.service;

import com.example.peakly.domain.focusSession.dto.result.DisruptionReasonDTO;
import com.example.peakly.domain.focusSession.dto.response.DisruptionReasonListResponse;
import com.example.peakly.domain.focusSession.entity.DisruptionReason;
import com.example.peakly.domain.focusSession.repository.DisruptionReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisruptionReasonServiceImpl implements DisruptionReasonService {

    private final DisruptionReasonRepository disruptionReasonRepository;

    @Override
    @Transactional(readOnly = true)
    public DisruptionReasonListResponse getDisruptionReasons(Boolean activeOnly) {
        boolean onlyActive = activeOnly == null || activeOnly;

        List<DisruptionReason> reasons = onlyActive
                ? disruptionReasonRepository.findAllByActiveTrueOrderBySortOrderAsc()
                : disruptionReasonRepository.findAllByOrderBySortOrderAsc();

        List<DisruptionReasonDTO> dtos = reasons.stream()
                .map(r -> new DisruptionReasonDTO(
                        r.getId(),
                        r.getCode(),
                        r.getName(),
                        r.getSortOrder(),
                        r.isActive()
                ))
                .toList();

        return new DisruptionReasonListResponse(dtos);
    }
}
