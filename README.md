# PR 좀비 헌터 🧟

GitHub PR이 오래 방치되면 자동으로 감지해서 이메일로 알려주는 시스템입니다.

---

## 프로젝트 구조

```
pr-zombie-hunter/
├── gateway          # 외부 요청 라우팅 (:8090)
├── collector        # GitHub Webhook으로 PR 수집 (:8081)
├── grader           # PR 방치 일수 계산 및 등급 판정 (:8082)
├── notifier         # 이메일 알림 발송 (:8083)
├── graphql-service  # GraphQL API - 대시보드 데이터 제공 (:8084)
└── frontend         # Android 대시보드 앱 (Kotlin Compose)
```

---

## 좀비 등급 기준

| 등급 | 기준 | 설명 |
|------|------|------|
| NONE | 3일 미만 | 정상 |
| SEEDLING 🌱 | 3일 이상 | 새싹 좀비 |
| ZOMBIE 🧟 | 7일 이상 | 좀비 |
| BOSS 💀 | 14일 이상 | 보스 좀비 |

> `lastActivityAt` (마지막 활동일) 기준 경과일로 판정합니다.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Kotlin 2.2.21 |
| 프레임워크 | Spring Boot 4.0.6 |
| GraphQL | Spring GraphQL (spring-boot-starter-graphql) |
| DB | MySQL 8.0 + JPA + Flyway |
| 이메일 | Spring Mail (Gmail SMTP) |
| 인프라 | Docker Compose |
| CI | GitHub Actions |
| 테스트 | Kotest + Mockk |

---

## DB 스키마

```sql
pull_request          -- PR 메타데이터 + 좀비 등급
zombie_grade_history  -- 등급 변경 이력
hunter_action         -- 처치완료 기록
notifications         -- 이메일 발송 이력
```

---

## 로컬 실행 방법

### 사전 준비

- Docker Desktop 설치
- Gmail 앱 비밀번호 발급 (Google 계정 → 보안 → 2단계 인증 → 앱 비밀번호)

### 1. .env 파일 생성

프로젝트 루트에 `.env` 파일 생성 (`.env.example` 참고):

```
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
DB_URL=jdbc:mysql://mysql:3306/zombie_hunter?serverTimezone=Asia/Seoul
DB_USERNAME=root
DB_PASSWORD=root
WEBHOOK_SECRET=zombie_webhook_secret
MAIL_USERNAME=Gmail계정@gmail.com
MAIL_PASSWORD=앱비밀번호16자리
NOTIFY_RECIPIENTS=팀원1@gmail.com,팀원2@gmail.com,팀원3@gmail.com
```

### 2. 전체 서비스 실행

```bash
docker compose up --build
```

### 3. 접속 주소

| 서비스 | 주소 |
|--------|------|
| Gateway | http://localhost:8090 |
| Collector | http://localhost:8081 |
| Grader | http://localhost:8082 |
| Notifier | http://localhost:8083 |
| GraphQL Playground | http://localhost:8084/graphiql |

> 로컬에 MySQL이 이미 설치된 경우 포트 충돌 방지를 위해 `3307`로 설정되어 있습니다.

---

## GraphQL API

### Query

```graphql
# 전체 PR 조회
query {
  pullRequests {
    id
    title
    author
    zombieGrade
    staleDays
    lastActivityAt
  }
}

# 등급 필터 조회
query {
  pullRequests(zombieGrade: BOSS) {
    id
    title
    zombieGrade
    staleDays
  }
}

# 단건 조회
query {
  pullRequest(id: "owner/repo#42") {
    id
    title
    zombieGrade
  }
}
```

### Mutation

```graphql
# 처치완료
mutation {
  markAsHunted(
    prId: "owner/repo#42"
    hunterId: "홍길동"
    actionType: "HUNT"
  ) {
    id
    prId
    hunterId
    createdAt
  }
}
```

---

## 이메일 알림

좀비 PR 발견 시 `NOTIFY_RECIPIENTS`에 등록된 팀원 전원에게 발송됩니다.

| 등급 | 제목 |
|------|------|
| SEEDLING | [🌱 새싹 좀비] PR제목 — 슬슬 신경 써주세요 |
| ZOMBIE | [🧟 좀비 PR] PR제목 — 방치된 지 7일이 넘었습니다 |
| BOSS | [💀 보스 좀비 발견!] PR제목 — 즉시 처치가 필요합니다 |

**중복 발송 방지:** 같은 PR + 같은 등급 조합으로 이미 발송한 경우 재발송하지 않습니다.

---

## CI

PR을 올리거나 main에 push하면 GitHub Actions가 5개 모듈을 병렬로 자동 빌드/테스트합니다.

```
PR 생성 / main push
        ↓
GitHub Actions CI
        ↓
5개 모듈 동시 빌드 + 테스트
(gateway, collector, grader, notifier, graphql-service)
```

---

## 브랜치 전략

```
main
└── feature
    └── init-core-setup   ← 현재 작업 브랜치
```

---

## 프론트엔드 연동 (Android)

Android 에뮬레이터에서 백엔드 접속 시:

```
GraphQL 주소: http://10.0.2.2:8090/graphql
(에뮬레이터에서 localhost 대신 10.0.2.2 사용)
```

Apollo Client로 GraphQL 연동 필요.

---

## 자주 쓰는 명령어

```bash
# 전체 실행
docker compose up --build

# 특정 모듈만 재빌드
docker compose up --build notifier

# 로그 확인
docker compose logs -f graphql-service

# 전체 종료
docker compose down

# 실행 중인 컨테이너 확인
docker ps
```
