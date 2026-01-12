package com.finops.collector.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    @Profile("!local")
    public CostExplorerClient costExplorerClient() {
        log.info("AWS Cost Explorer Client 생성 - Region: {}", region);

        return CostExplorerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("local")
    public CostExplorerClient mockCostExplorerClient() {
        log.info("Mock Cost Explorer Client 생성 (로컬 환경)");
        // 로컬 환경에서는 null 반환 - MockCostCollectorService 사용
        return null;
    }
}
