# PR 좀비 헌터

GitHub PR이 오래 방치되면 자동으로 감지해서 이메일로 알려주는 시스템입니다.

## 프로젝트 구조

```
pr-zombie-hunter/
├── collector        # GitHub API로 PR 목록 수집
├── grader          # PR 방치 일수 계산 및 등급 판정
├── graphql-service # 프론트엔드 데이터 조회 API (graphql-kotlin)
├── notifier        # 이메일 알림 발송 (Gmail SMTP)
└── gateway         # 외부 요청 라우팅
```

## 좀비 등급 기준

| 등급 | 기준 | 설명 |
|------|------|------|
| NONE | 3일 미만 | 정상 |
| SEEDLING | 3일 이상 | 새싹 좀비 |
| ZOMBIE | 7일 이상 | 좀비 |
| BOSS | 14일 이상 | 보스 좀비 |

> 기준은 PR의 `updatedAt` 기준 경과일입니다.

## 기술 스택

- **언어:** Kotlin 2.2.21
- **프레임워크:** Spring Boot 4.0.6
- **GraphQL:** graphql-kotlin-spring-server 8.4.0 (Expedia Group)
- **DB:** MySQL 8.0 + JPA + Flyway
- **이메일:** Spring Mail (Gmail SMTP)
- **CI:** GitHub Actions

## 로컬 실행

### 사전 준비

- JDK 17
- Docker Desktop

### MySQL 실행

```bash
docker compose up mysql -d
```

> 로컬에 MySQL이 이미 설치된 경우 포트 충돌을 피하기 위해 `3307`로 설정되어 있습니다.

### 환경변수 설정

`notifier` 모듈 실행 시 아래 환경변수가 필요합니다.

```
MAIL_USERNAME=Gmail계정@gmail.com
MAIL_PASSWORD=Gmail앱비밀번호16자리
```

> Gmail 앱 비밀번호는 Google 계정 → 보안 → 2단계 인증 → 앱 비밀번호에서 발급합니다.

### DB 스키마

Spring Boot 실행 시 Flyway가 자동으로 테이블을 생성합니다.

```
repositories   # 모니터링할 레포 목록
pull_requests  # 수집된 PR 및 등급 정보
```

## 브랜치 전략

```
main
└── feature/init-core-setup   # 핵심 세팅 (graphql-kotlin, SMTP, DB, 등급 기준)
```

## CI

PR을 올리거나 main에 push하면 GitHub Actions가 자동으로 5개 모듈을 병렬 빌드/테스트합니다.
