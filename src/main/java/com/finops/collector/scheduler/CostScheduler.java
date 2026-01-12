package com.finops.collector.scheduler;

import com.finops.collector.service.CostCollectorService;
import com.finops.collector.service.MockCostCollectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class CostScheduler {

    private final CostCollectorService costCollectorService;
    private final MockCostCollectorService mockCostCollectorService;

    @Autowired
    public CostScheduler(
            @Autowired(required = false) CostCollectorService costCollectorService,
            @Autowired(required = false) MockCostCollectorService mockCostCollectorService
    ) {
        this.costCollectorService = costCollectorService;
        this.mockCostCollectorService = mockCostCollectorService;
    }

    /**
     * 매일 오전 9시에 전날 비용 데이터 수집
     */
    @Scheduled(cron = "${collector.schedule.cron:0 0 9 * * *}")
    public void collectYesterdayCost() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("스케줄 실행 - 전날 비용 수집: {}", yesterday);

        collectCosts(yesterday);
    }

    /**
     * 애플리케이션 시작 시 최근 7일 데이터 수집
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void collectRecentCosts() {
        log.info("초기 데이터 수집 시작 - 최근 7일");

        LocalDate today = LocalDate.now();
        for (int i = 7; i >= 1; i--) {
            LocalDate date = today.minusDays(i);
            collectCosts(date);
        }

        log.info("초기 데이터 수집 완료");
    }

    private void collectCosts(LocalDate date) {
        try {
            if (mockCostCollectorService != null) {
                // 로컬 환경
                mockCostCollectorService.collectDailyCost(date);
                mockCostCollectorService.collectServiceCosts(date);
            } else if (costCollectorService != null) {
                // AWS 환경
                costCollectorService.collectDailyCost(date);
                costCollectorService.collectServiceCosts(date);
            }
        } catch (Exception e) {
            log.error("비용 수집 실패: {}", date, e);
        }
    }
}
