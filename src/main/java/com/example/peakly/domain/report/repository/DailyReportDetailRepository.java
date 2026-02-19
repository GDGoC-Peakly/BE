package com.example.peakly.domain.report.repository;

import com.example.peakly.domain.report.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyReportDetailRepository extends JpaRepository<DailyReport,Long> {

    Optional<DailyReport> findByUserIdAndReportDate(Long userId, LocalDate reportDate);
}
