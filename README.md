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

## 좀비 몬스터 HP 시스템 (Grader)

기존의 시간 기반 등급제(Enum)를 폐기하고, PR 방치 상태를 시각화하고 재미를 주는 **HP 몬스터 시스템**과 **Redis 캐싱**을 도입했습니다.

### 핵심 비즈니스 규칙

1. **초기 HP 설정:** PR 생성 시 기본적으로 **10,000 HP**를 가진 좀비 몬스터가 오픈 상태(`OPEN`)로 생성됩니다.
2. **몬스터 성장 (Grow):** 스케줄러와 연동되어 **6시간**마다 활성화된 몬스터(`OPEN` 상태 및 미처치 상태)의 HP가 **2배**로 증가하고 Redis에 캐싱됩니다.
3. **데미지 적용 (Damage):** PR에 **유니크 코멘트**가 작성되면 몬스터에게 **5,000 HP** 데미지를 줍니다. (중복 코멘트는 데미지를 주지 않음)
4. **몬스터 처치 (Defeat):** HP가 **0 이하**가 되거나, PR의 상태가 **MERGED** 또는 **CLOSED**로 변경되면 즉시 처치(`isDefeated = true`, `currentHp = 0`)됩니다.
5. **몬스터 복원 (Revert & Redis Backup):** PR이 **REVERTED** 상태로 변경될 경우, Redis 백업 공간(`hp_before_defeat`)에서 처치 직전의 HP를 불러와 안전하게 복원하고 `isDefeated = false`로 되돌립니다.


## 기술 스택

- **언어:** Kotlin 2.2.21
- **프레임워크:** Spring Boot 4.0.6
- **GraphQL:** graphql-kotlin-spring-server 8.4.0 (Expedia Group)
- **DB:** MySQL 8.0 + JPA + Flyway
- **캐싱 및 백업:** Redis (몬스터 HP 실시간 캐싱 및 복원용 백업 저장소)
- **이메일:** Spring Mail (Gmail SMTP)
- **CI:** GitHub Actions

## 로컬 실행

### 사전 준비

- JDK 17
- Docker Desktop (MySQL 및 Redis 실행용)

### MySQL 및 Redis 실행

```bash
docker compose up mysql redis -d
```

> - 로컬에 MySQL이 이미 설치된 경우 포트 충돌을 피하기 위해 MySQL 포트는 `3307`로 설정되어 있습니다.
> - Redis는 기본 포트인 `6379`로 실행됩니다.

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
