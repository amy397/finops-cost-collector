package com.finops.collector.service;

import com.finops.collector.entity.DailyCost;
import com.finops.collector.entity.ServiceCost;
import com.finops.collector.repository.DailyCostRepository;
import com.finops.collector.repository.ServiceCostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Profile("!local")
@RequiredArgsConstructor
public class CostCollectorService {

    private final CostExplorerClient costExplorerClient;
    private final DailyCostRepository dailyCostRepository;
    private final ServiceCostRepository serviceCostRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public void collectDailyCost(LocalDate date) {
        log.info("일별 비용 수집 시작: {}", date);

        try {
            var request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(date.format(DATE_FORMAT))
                            .end(date.plusDays(1).format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .build();

            var response = costExplorerClient.getCostAndUsage(request);

            response.resultsByTime().forEach(result -> {
                var cost = result.total().get("UnblendedCost");
                BigDecimal amount = new BigDecimal(cost.amount());

                saveDailyCost(date, amount, cost.unit());
            });

            log.info("일별 비용 수집 완료: {}", date);
        } catch (Exception e) {
            log.error("일별 비용 수집 실패: {}", date, e);
            throw new RuntimeException("비용 수집 실패", e);
        }
    }

    @Transactional
    public void collectServiceCosts(LocalDate date) {
        log.info("서비스별 비용 수집 시작: {}", date);

        try {
            var request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(date.format(DATE_FORMAT))
                            .end(date.plusDays(1).format(DATE_FORMAT))
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type(GroupDefinitionType.DIMENSION)
                            .key("SERVICE")
                            .build())
                    .build();

            var response = costExplorerClient.getCostAndUsage(request);

            // 기존 데이터 삭제 후 새로 저장
            serviceCostRepository.deleteByCostDate(date);

            response.resultsByTime().forEach(result -> {
                result.groups().forEach(group -> {
                    String serviceName = group.keys().getFirst();
                    var cost = group.metrics().get("UnblendedCost");
                    BigDecimal amount = new BigDecimal(cost.amount());

                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        saveServiceCost(date, serviceName, amount, cost.unit());
                    }
                });
            });

            log.info("서비스별 비용 수집 완료: {}", date);
        } catch (Exception e) {
            log.error("서비스별 비용 수집 실패: {}", date, e);
            throw new RuntimeException("서비스별 비용 수집 실패", e);
        }
    }

    private void saveDailyCost(LocalDate date, BigDecimal amount, String currency) {
        dailyCostRepository.findByCostDate(date)
                .ifPresentOrElse(
                        existing -> {
                            existing.setTotalCost(amount);
                            existing.setCurrency(currency);
                            dailyCostRepository.save(existing);
                            log.debug("일별 비용 업데이트: {} - {}", date, amount);
                        },
                        () -> {
                            var dailyCost = DailyCost.builder()
                                    .costDate(date)
                                    .totalCost(amount)
                                    .currency(currency)
                                    .build();
                            dailyCostRepository.save(dailyCost);
                            log.debug("일별 비용 저장: {} - {}", date, amount);
                        }
                );
    }

    private void saveServiceCost(LocalDate date, String serviceName, BigDecimal amount, String currency) {
        var serviceCost = ServiceCost.builder()
                .costDate(date)
                .serviceName(serviceName)
                .cost(amount)
                .currency(currency)
                .build();
        serviceCostRepository.save(serviceCost);
        log.debug("서비스 비용 저장: {} - {} - {}", date, serviceName, amount);
    }
}
