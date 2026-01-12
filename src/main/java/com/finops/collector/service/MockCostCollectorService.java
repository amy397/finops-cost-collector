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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@Profile("local")
@RequiredArgsConstructor
public class MockCostCollectorService {

    private final DailyCostRepository dailyCostRepository;
    private final ServiceCostRepository serviceCostRepository;

    private static final List<String> AWS_SERVICES = List.of(
            "Amazon EC2", "Amazon S3", "Amazon RDS",
            "AWS Lambda", "Amazon CloudFront", "Amazon DynamoDB",
            "Amazon EKS", "AWS Fargate", "Amazon ElastiCache"
    );

    private final Random random = new Random();

    @Transactional
    public void collectDailyCost(LocalDate date) {
        log.info("[MOCK] 일별 비용 수집: {}", date);

        if (dailyCostRepository.existsByCostDate(date)) {
            log.debug("[MOCK] 이미 존재하는 날짜: {}", date);
            return;
        }

        BigDecimal totalCost = generateDailyCost(date);

        var dailyCost = DailyCost.builder()
                .costDate(date)
                .totalCost(totalCost)
                .currency("USD")
                .build();

        dailyCostRepository.save(dailyCost);
        log.info("[MOCK] 일별 비용 저장 완료: {} - ${}", date, totalCost);
    }

    @Transactional
    public void collectServiceCosts(LocalDate date) {
        log.info("[MOCK] 서비스별 비용 수집: {}", date);

        // 기존 데이터가 있으면 스킵
        if (!serviceCostRepository.findByCostDate(date).isEmpty()) {
            log.debug("[MOCK] 이미 존재하는 날짜: {}", date);
            return;
        }

        for (String service : AWS_SERVICES) {
            BigDecimal cost = generateServiceCost(service);

            var serviceCost = ServiceCost.builder()
                    .costDate(date)
                    .serviceName(service)
                    .cost(cost)
                    .currency("USD")
                    .build();

            serviceCostRepository.save(serviceCost);
        }

        log.info("[MOCK] 서비스별 비용 저장 완료: {}", date);
    }

    private BigDecimal generateDailyCost(LocalDate date) {
        double baseCost = 100 + random.nextDouble() * 50;

        // 주말은 비용 감소
        if (date.getDayOfWeek().getValue() >= 6) {
            baseCost *= 0.7;
        }

        return BigDecimal.valueOf(baseCost).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal generateServiceCost(String service) {
        double baseCost = switch (service) {
            case "Amazon EC2" -> 30 + random.nextDouble() * 20;
            case "Amazon RDS" -> 20 + random.nextDouble() * 15;
            case "Amazon S3" -> 10 + random.nextDouble() * 10;
            case "Amazon EKS" -> 15 + random.nextDouble() * 10;
            case "AWS Lambda" -> 5 + random.nextDouble() * 5;
            default -> 3 + random.nextDouble() * 7;
        };
        return BigDecimal.valueOf(baseCost).setScale(4, RoundingMode.HALF_UP);
    }
}
