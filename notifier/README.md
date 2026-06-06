# Notifier 서비스

PR 좀비 헌터의 이메일 알림 담당 서비스입니다.
RabbitMQ로 몬스터 이벤트를 수신하고, 팀원 전원에게 HP 현황 이메일을 발송합니다.

---

## 담당 기능

| 기능 | 설명 |
|------|------|
| 이벤트 수신 | RabbitMQ `monster.events` 큐에서 grader 메시지 수신 |
| 즉시 알림 | 처치 완료(defeated), 부활(revived) 시 즉시 이메일 발송 |
| 정기 알림 | 매 정각(1시간마다) 살아있는 몬스터 HP 현황 이메일 발송 |
| 중복 방지 | 같은 PR + 같은 이벤트 타입 중복 발송 차단 |
| 이력 저장 | 발송 내역을 `notifications` 테이블에 기록 |

---

## 아키텍처

```
grader
  │
  │ monster.events (fanout exchange)
  │   - hp_updated : 6시간마다 HP 성장 완료 시
  │   - defeated   : 처치 완료 시
  │   - revived    : Revert 부활 시
  ▼
[RabbitMQ]
  │
  ▼
MonsterEventConsumer
  │
  ├── hp_updated → MonsterHpCache 갱신
  ├── defeated   → 캐시 제거 + 즉시 이메일 발송
  └── revived    → 캐시 추가 + 즉시 이메일 발송
  │
  ▼
MonsterHpCache (인메모리)
  │
  ▼ (매 정각)
HourlyNotifierScheduler
  │
  ▼
MailService → Gmail SMTP → 팀원 전원
```

---

## 이메일 종류

### 1시간 정기 발송 (hp_updated)

**제목:** `[🧟 좀비 PR] PR제목 — HP 20000 (코멘트 4개 필요)`

**본문:**
```
[🧟 PR 좀비 헌터 — 시간별 현황 보고]

현재 N마리의 좀비 PR이 생존 중입니다.

• PR: feat/auth-refactor
  현재 HP: 20000 / 40000
  처치까지 필요한 코멘트: 4개 (코멘트 1개 = 5,000 데미지)
  링크: https://github.com/...

⚠️ PR HP는 6시간마다 2배로 증가합니다.
```

### 처치 완료 즉시 발송 (defeated)

**제목:** `[🎉 처치 완료] PR제목 — 팀워크로 처치했습니다!`

### 부활 즉시 발송 (revived)

**제목:** `[💀 몬스터 부활!] PR제목 — Revert로 좀비가 되살아났습니다`

---

## 파일 구조

```
notifier/src/main/kotlin/com/zombie/notifier/
├── config/
│   └── RabbitMQConfig.kt           # 큐/익스체인지 설정
├── domain/
│   ├── Notification.kt             # 발송 이력 엔티티
│   └── NotificationRepository.kt   # DB 조회 인터페이스
├── mail/
│   ├── MailService.kt              # 실제 이메일 발송
│   ├── ZombieMailTemplate.kt       # 이벤트별 이메일 템플릿
│   └── ZombieNotifierService.kt    # 발송 조건 판단 + 이력 저장
├── messaging/
│   ├── MonsterEvent.kt             # RabbitMQ 메시지 형식
│   ├── MonsterEventConsumer.kt     # 큐 구독자
│   └── MonsterHpCache.kt          # HP 인메모리 캐시
└── scheduler/
    └── HourlyNotifierScheduler.kt  # 1시간 정기 발송
```

---

## 환경변수

| 변수 | 설명 | 예시 |
|------|------|------|
| `MAIL_USERNAME` | Gmail 계정 | `your@gmail.com` |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 (16자리) | `xxxx xxxx xxxx xxxx` |
| `NOTIFY_RECIPIENTS` | 수신자 이메일 목록 (쉼표 구분) | `a@gmail.com,b@gmail.com` |
| `RABBITMQ_HOST` | RabbitMQ 호스트 | `rabbitmq` (Docker) / `localhost` (로컬) |
| `DB_URL` | MySQL 연결 URL | `jdbc:mysql://mysql:3306/zombie_hunter` |

---

## REST API

### `POST /notify`

외부(또는 테스트)에서 직접 알림을 트리거하는 엔드포인트입니다.  
RabbitMQ 없이도 이메일 발송을 수동으로 테스트할 수 있습니다.

**Request Body**

```json
{
  "prId": "repo#42",
  "prTitle": "feat/auth-refactor",
  "prUrl": "https://github.com/org/repo/pull/42",
  "staleDays": 7,
  "grade": "zombie"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `prId` | String | GitHub PR 식별자 |
| `prTitle` | String | PR 제목 |
| `prUrl` | String | PR 링크 |
| `staleDays` | Long | PR이 방치된 일수 |
| `grade` | String | 좀비 등급 (zombie / ancient / lich 등) |

**Response**

```json
{ "status": "ok" }
```

**curl 예시**

```bash
curl -X POST http://localhost:8083/notify \
  -H "Content-Type: application/json" \
  -d '{
    "prId": "repo#42",
    "prTitle": "feat/auth-refactor",
    "prUrl": "https://github.com/org/repo/pull/42",
    "staleDays": 7,
    "grade": "zombie"
  }'
```

---

## RabbitMQ

### Exchange / Queue 구조

```
grader
  │
  │  [monster.events] — fanout exchange
  │   routing key 무관, 바인딩된 모든 큐에 전달
  ▼
[notifier.monster.queue]
  │
  ▼
MonsterEventConsumer
```

| 항목 | 값 |
|------|------|
| Exchange | `monster.events` (fanout, durable) |
| Queue | `notifier.monster.queue` (durable) |
| 관리자 UI | http://localhost:15672 (ID: guest / PW: guest) |

### 메시지 형식 (`MonsterEvent`)

grader가 `monster.events` exchange로 발행하는 메시지 구조입니다.

```json
{
  "eventType": "hp_updated",
  "prId": "repo#42",
  "prTitle": "feat/auth-refactor",
  "prUrl": "https://github.com/org/repo/pull/42",
  "currentHp": 20000,
  "maxHp": 40000,
  "requiredComments": 4
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `eventType` | String | `hp_updated` / `defeated` / `revived` |
| `prId` | String | GitHub PR 식별자 |
| `prTitle` | String | PR 제목 |
| `prUrl` | String | PR 링크 |
| `currentHp` | Int | 현재 HP |
| `maxHp` | Int | 최대 HP (6시간마다 2배 성장) |
| `requiredComments` | Int | 처치까지 필요한 코멘트 수 (currentHp / 5000, 올림) |

### 이벤트 타입별 처리 흐름

| eventType | 처리 |
|-----------|------|
| `hp_updated` | `MonsterHpCache` 갱신 → 1시간 스케줄러가 이메일 발송 |
| `defeated` | 캐시 제거 + 즉시 이메일 발송 |
| `revived` | 캐시 재추가 + 즉시 이메일 발송 |

### MonsterHpCache (인메모리)

`hp_updated` 이벤트를 `ConcurrentHashMap<prId, MonsterEvent>` 형태로 캐싱합니다.  
`HourlyNotifierScheduler`가 매 정각 이 캐시를 읽어 살아있는 몬스터 현황을 이메일로 발송합니다.

```
hp_updated 수신 → cache[prId] = event
defeated   수신 → cache.remove(prId)
매 정각          → cache.values → 이메일 발송
```

---

## DB 테이블

### notifications

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT | PK |
| pull_request_id | VARCHAR(50) | GitHub PR 식별자 |
| recipient_email | VARCHAR(255) | 발송된 수신자 목록 |
| grade | VARCHAR(20) | 이벤트 타입 (hp_updated / defeated / revived) |
| sent_at | DATETIME | 발송 시각 |

---

## 테스트

### 테스트 크기 분류

| 파일 | 크기 | 설명 |
|------|------|------|
| `MailServiceTest` | Small | JavaMailSender Mock — 발송 메커니즘 검증 |
| `ZombieMailTemplateTest` | Small | 순수 함수 — 이벤트별 제목/본문 생성 검증 |
| `ZombieNotifierServiceTest` | Small | Mock DB/Mail — 발송 조건 분기 검증 |
| `HourlyNotifierSchedulerTest` | Small | Mock Cache/Mail — 스케줄러 발송 조건 검증 |
| `NotificationRepositoryTest` | Medium | H2 인메모리 DB — 실제 SQL 쿼리 동작 검증 |

### 테스트 실행

```bash
cd notifier
./gradlew test
```

---

## 로컬 실행

```bash
# 전체 서비스와 함께 실행
docker compose up --build notifier

# RabbitMQ 관리자 UI 접속
http://localhost:15672

# 이메일 발송 테스트 (수동)
curl -X POST http://localhost:8083/notify \
  -H "Content-Type: application/json" \
  -d '{"eventType":"hp_updated","prId":"repo#42","prTitle":"feat/test","prUrl":"https://github.com","currentHp":20000,"maxHp":40000,"requiredComments":4}'
```
