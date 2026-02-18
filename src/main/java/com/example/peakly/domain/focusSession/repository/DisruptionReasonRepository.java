package com.example.peakly.domain.focusSession.repository;

import com.example.peakly.domain.focusSession.entity.DisruptionReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisruptionReasonRepository extends JpaRepository<DisruptionReason, Long> {

    List<DisruptionReason> findAllByActiveTrueOrderBySortOrderAsc();

    List<DisruptionReason> findAllByOrderBySortOrderAsc();
}
