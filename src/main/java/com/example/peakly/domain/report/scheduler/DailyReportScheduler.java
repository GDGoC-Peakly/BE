package com.example.peakly.domain.report.scheduler;

import com.example.peakly.domain.report.service.daily.DailyReportUpdateService;
import com.example.peakly.domain.user.entity.User;
import com.example.peakly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final DailyReportUpdateService dailyReportUpdateService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 5 * * *") // 매일 새벽 5시
    public void calculateYesterdayReports() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("일간 리포트 배치 시작 - 대상 날짜: {}", yesterday);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                dailyReportUpdateService.updateReport(user, yesterday);
            } catch (Exception e) {
                log.warn("리포트 계산 실패 userId={}, date={}", user.getId(), yesterday, e);
            }
        }

        log.info("일간 리포트 배치 완료 - 대상 날짜: {}", yesterday);
    }
}