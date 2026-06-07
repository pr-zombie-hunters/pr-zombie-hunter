# 🧟 PR Zombie Hunter

> 방치된 Pull Request를 몬스터로 규정하고, HP 시스템으로 팀이 협력해서 처치하는 시스템

팀 프로젝트 | 모바일최신기술 기말 | Kotlin 기반 MSA

---

## 📌 프로젝트 개요

GitHub 레포지토리에서 머지되지 않고 방치된 PR을 **몬스터**로 규정합니다.
PR이 방치될수록 몬스터 HP가 성장하며, 팀원이 코멘트를 달면 데미지를 줄 수 있습니다.
HP가 0 이하로 떨어지거나 PR이 머지/클로즈되면 몬스터가 처치됩니다.

### 몬스터 HP 시스템

| 시점 | HP | 비고 |
|---|---|---|
| PR 방치 시 | 10,000 | 몬스터 생성 |
| 6시간 경과마다 | 현재 HP × 2 | 자동 성장 |
| 팀원 코멘트 | -5,000 | 1인 1회 제한 |
| HP ≤ 0 또는 머지/클로즈 | 처치 완료 | |
| PR Revert | 몬스터 부활 | 처치 직전 HP로 복원 |

---

## 🏗️ 시스템 아키텍처

```
GitHub PR/코멘트/Revert 이벤트
    ↓ Webhook
API Gateway (:8080) ← JWT 인증 · 라우팅
    ↓
[Collector :8081] → MySQL (:3306)
                         ↓ 6시간마다
                    [Grader :8082] → HP 성장 · 데미지 반영
                         ↓ 1시간마다
                    [Notifier :8083] → 이메일 알림
                         ↓
                    [GraphQL :8084] ← Kotlin 대시보드
```

### MSA 서비스 구성

| 서비스 | 포트 | 담당 | 핵심 역할 |
|---|---|---|---|
| API Gateway | :8080 | PL+PM | JWT 인증, 라우팅, Rate Limit |
| **Collector** | **:8081** | **BA (조혜연)** | **GitHub 이벤트 수신, PR/코멘트/Revert 처리** |
| Grader | :8082 | BB (김관혁) | 6시간 HP 성장, 데미지 반영 |
| Notifier | :8083 | BC (성수연) | 1시간 정기 이메일 발송 |
| GraphQL | :8084 | BC (성수연) | 대시보드 데이터 제공 |

---

## 🛠️ 기술 스택

| 분류 | 기술 | 선택 이유 |
|---|---|---|
| 언어 | Kotlin | 간결한 문법, Null 안전성 |
| 프레임워크 | Spring Boot 4.x | 빠른 MSA 구성 |
| 데이터베이스 | MySQL 8.0 | 관계형 이력 관리 |
| 캐시 | Redis 7 | 중복 데미지 방지 빠른 조회 |
| API | GraphQL | 등급별 필터 쿼리, 오버패칭 방지 |
| 동시성 | Java 21 가상 스레드 | PR 대량 처리 시 OS 스레드 부담 최소화 |
| 컨테이너 | Docker Compose | 전 서비스 단일 명령 실행 |
| CI/CD | GitHub Actions + Railway | PR 머지 시 자동 테스트 및 배포 |
| 알림 | JavaMail SMTP (Gmail) | 1시간 정기 이메일 발송 |
| 이슈 관리 | Jira | Epic > Story > Task > Spike 계층 관리 |

---

## 🎯 Collector 서비스 상세 (BA 담당: 조혜연)

### 역할

GitHub에서 발생하는 PR 관련 이벤트를 실시간으로 수신하고 MySQL에 저장합니다.
코멘트 이벤트를 수신해 Redis 중복 체크 후 damage_log에 기록합니다.
PR Revert 이벤트를 감지해 Grader의 몬스터 부활 트리거를 전달합니다.

### 처리하는 Webhook 이벤트

| 이벤트 | action | 처리 내용 |
|---|---|---|
| PR 생성 | opened | MySQL pull_requests 테이블 신규 저장 |
| PR 재오픈 | reopened | MySQL pull_requests 테이블 신규 저장 |
| PR 닫힘 | closed | 해당 PR 상태 KILLED로 업데이트 |
| PR Revert | reopened (제목 Revert) | 몬스터 부활 트리거 → Grader 전달 |
| 코멘트 생성 | issue_comment created | Redis 중복 확인 → damage_log 저장 |
| 중복 코멘트 | — | Redis 키 확인 후 스킵 |

### 구현 파일 목록

| 파일 | 역할 |
|---|---|
| `WebhookController.kt` | Webhook 엔드포인트, Secret 검증(HMAC-SHA256) |
| `SecurityConfig.kt` | OAuth2 로그인 설정, Webhook 인증 예외 처리 |
| `CollectorService.kt` | PR 이벤트 분기 처리, Revert 감지 |
| `DamageService.kt` | Redis 중복 체크, damage_log 저장 |
| `PullRequest.kt` | pull_requests 테이블 엔티티 |
| `PullRequestRepository.kt` | PR 저장/조회 |
| `DamageLog.kt` | damage_log 테이블 엔티티 |
| `DamageLogRepository.kt` | 데미지 로그 저장/조회 |

### DB 테이블

**pull_requests**

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | 식별자 |
| pr_number | INT | GitHub PR 번호 (중복 스킵 기준) |
| title | VARCHAR | PR 제목 (알림 메시지에 포함) |
| author | VARCHAR | 작성자 GitHub ID |
| repo_full_name | VARCHAR | 레포 이름 |
| html_url | VARCHAR | PR 링크 (알림에 포함) |
| state | VARCHAR | OPEN / KILLED |
| last_activity_at | DATETIME | 방치 기간 계산 기준 (updated_at) |
| zombie_grade | VARCHAR | NONE / SPROUT / ZOMBIE / BOSS |
| created_at | DATETIME | 저장 시각 |

**damage_log**

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | 식별자 |
| pr_id | BIGINT FK | 대상 PR |
| attacker_github_id | VARCHAR | 코멘트 작성자 GitHub ID |
| damage_amount | INT | 데미지량 (고정 5,000) |
| comment_id | VARCHAR | GitHub 코멘트 ID (중복 방지) |
| attacked_at | DATETIME | 데미지 발생 시각 |

### Redis 키 구조

| 키 | 용도 |
|---|---|
| `damage:pr{id}:user{githubId}` | 중복 코멘트 방지 (1인 1회 제한) |

---

## 🔐 GitHub OAuth 연동

1. [GitHub Developer Settings](https://github.com/settings/developers)에서 OAuth App 등록
2. Callback URL: `http://localhost:8081/login/oauth2/code/github`
3. 발급받은 값을 `.env` 파일에 입력

```properties
# application.properties
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=repo,read:org
```

> ⚠️ `.env` 파일은 `.gitignore`에 등록되어 GitHub에 올라가지 않습니다.
> 팀원 공유는 `.env.example` 파일을 참고하세요.

---

## 🪝 GitHub Webhook 연동

GitHub 레포 → Settings → Webhooks → Add webhook

| 항목 | 값 |
|---|---|
| Payload URL | `http://localhost:8081/webhook/github` |
| Content type | `application/json` |
| Secret | `.env`의 WEBHOOK_SECRET 값 |
| 이벤트 | Pull requests, Issue comments |

**Secret 검증:** `X-Hub-Signature-256` 헤더로 HMAC-SHA256 검증.
검증 실패 시 HTTP 400 반환, DB 저장 없음.

---

## 🧪 TDD 케이스

| 이슈 | 테스트 내용 | 결과 |
|---|---|---|
| SCRUM-50 | 중복 PR 입력 → 스킵 처리 | ✅ 통과 |
| SCRUM-79 | 중복 PR 스킵 테스트 완성 | ✅ 통과 |
| SCRUM-112 | GitHub API 정상 응답 시 PR 수집 | ✅ 설계 완료 |
| SCRUM-113 | GitHub API 500 오류 처리 | ✅ 설계 완료 |
| SCRUM-114 | GitHub API Rate Limit 초과 처리 | ✅ 설계 완료 |
| SCRUM-115 | closed 상태 PR 저장 | ✅ 설계 완료 |
| NEW-07 | 코멘트 → 데미지 5,000 정상 처리 | ✅ 설계 완료 |
| NEW-08 | 중복 코멘트 → 데미지 스킵 | ✅ 설계 완료 |

---

## 🐳 Docker 실행 방법

```bash
# 1. .env 파일 생성 (.env.example 참고)
cp .env.example .env
# 값 입력 후 저장

# 2. MySQL + Redis 컨테이너 실행
docker-compose up mysql redis -d

# 3. Collector 앱 실행 (IntelliJ 또는 터미널)
.\gradlew.bat :collector:bootRun
```

---

## 📣 알림 정책

| 주기 | 내용 |
|---|---|
| 1시간마다 | 생존 중인 몬스터 목록, 현재 HP, 처치까지 필요한 코멘트 수 |

이메일 본문: **현재 HP + PR 제목 + GitHub PR 링크** 포함

---

## 👥 팀원 역할 분담

| 역할 | 담당자 | 담당 서비스 |
|---|---|---|
| PL+PM | 송윤서 | API Gateway, CI/CD, 통합 테스트 |
| BA | 조혜연 | Collector :8081 |
| BB | 김관혁 | Grader :8082 |
| BC | 성수연 | Notifier :8083, GraphQL :8084 |
| DB | 지현 | ERD 설계, 마이그레이션 |
| FE | 최소영 | Kotlin 대시보드 |

---

## 🐛 트러블슈팅

| 문제 | 원인 | 해결 |
|---|---|---|
| spring-dotenv 환경변수 미적용 | Spring Boot 4.x 비호환 | `springboot4-dotenv:5.0.1`로 교체 |
| jackson-module-kotlin import 오류 | 그룹 ID 오류 (`tools.jackson` → `com.fasterxml.jackson`) | 의존성 그룹 ID 수정 |
| SecurityConfig.kt 미인식 | 패키지 외부에 파일 생성 | `src/main/kotlin/com/zombie/collector/`로 이동 |
| docker-compose.yml Redis 오류 | `volumes` 블록 안에 `redis` 서비스 정의됨 | `services` 블록 안으로 이동, 들여쓰기 수정 |

