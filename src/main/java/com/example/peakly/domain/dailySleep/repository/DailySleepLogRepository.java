package com.example.peakly.domain.dailySleep.repository;

import com.example.peakly.domain.dailySleep.entity.DailySleepLog;
import com.example.peakly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySleepLogRepository extends JpaRepository<DailySleepLog, Long> {

    // 특정 사용자의 특정 날짜 기록 찾기
    Optional<DailySleepLog> findByUserAndBaseDate(User user, LocalDate baseDate);

    List<DailySleepLog> findByUser_IdAndBaseDateBetweenOrderByBaseDateAsc(Long userId, LocalDate from, LocalDate to);

}
