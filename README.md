# FinOps Cost Collector

AWS Cost Explorer API를 통해 비용 데이터를 수집하는 Spring Boot 서비스

## 기능

- 일별 AWS 비용 데이터 수집
- 서비스별 비용 분류
- PostgreSQL에 데이터 저장
- Kubernetes CronJob으로 스케줄 실행

## 기술 스택

- Java 17
- Spring Boot 3.2
- AWS SDK for Java v2
- PostgreSQL
- Docker

## 로컬 실행

```bash
./gradlew bootRun
```

## Docker 빌드

```bash
docker build -t finops-cost-collector .
```

## 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| SPRING_DATASOURCE_URL | DB 접속 URL | jdbc:postgresql://localhost:5432/finops |
| AWS_REGION | AWS 리전 | ap-northeast-2 |
