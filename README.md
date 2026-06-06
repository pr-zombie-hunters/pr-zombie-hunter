# 🧟 PR Zombie Hunter 기획서
팀 프로젝트 · 8인 · 2025년 5월~6월
v2 — HP 몬스터 시스템 + RabbitMQ 도입


> 
  **⚡ 컨셉 변경 요약 (v1 → v2)**
> 
  기존 **3·7·14일 등급제**를 **HP 몬스터 시스템**으로 전환하고, 서비스 간 통신을 REST 직접 호출에서 **RabbitMQ 메시지 큐**로 교체합니다.


## 1. 프로젝트 개요
PR Zombie Hunter는 GitHub에서 PR에 코멘트를 달거나 병합하는 과정에 게임 요소를 더한 협업 도구다. 방치된 PR은 시간이 지날수록 HP가 폭발적으로 커지는 몬스터가 되고, 팀원들은 코멘트로 데미지를 넣어 함께 처치한다. PR 관리를 의무가 아닌 팀 플레이로 만드는 것이 목적이다.


## 2. HP 몬스터 시스템 상세


### 2-1. 몬스터 HP 성장 규칙
PR이 방치되면 몬스터가 생성된다. 초기 HP는 **10,000**이며, 이후 **6시간마다 현재 HP × 2**로 자동 성장한다.

| 경과 시간 | HP | 비고 |
| --- | --- | --- |
| 0시간 | 10,000 | 몬스터 생성 |
| +6시간 | 20,000 | 1회 성장 |
| +12시간 | 40,000 | 2회 성장 |
| +18시간 | 80,000 | 3회 성장 |
| +24시간 | 160,000 | 4회 성장 |
| +30시간 | 320,000 | 5회 성장 🚨 |


### 2-2. 데미지 규칙
팀원이 해당 PR에 GitHub 코멘트를 달면 몬스터에게 **5,000 데미지**가 적용된다. 같은 PR에 같은 사람이 여러 번 코멘트를 달아도 데미지는 **1회만 인정**된다.

| 상황 | 이전 HP | 이후 HP | 비고 |
| --- | --- | --- | --- |
| 몬스터 생성 | — | 10,000 | PR 방치 시작 |
| 코멘트 1명 | 10,000 | 5,000 | 5,000 데미지 적용 |
| 6시간 경과 | 5,000 | 15,000 | 성장 (×2) 후 기존 데미지 유지 |
| 코멘트 2번째 | 15,000 | 10,000 | 두 번째 팀원 데미지 |
| PR 머지 | any | 처치 완료 | 즉시 처치 |
| Revert | 0 | 복원 | Redis `hp_before_defeat` 값으로 부활 |


### 2-3. 처치 완료 조건

| 조건 | 처리 방식 |
| --- | --- |
| HP ≤ 0 | 처치 완료 (팀원 협력 처치) |
| PR Merge | 즉시 처치 완료 |
| PR Close | 즉시 처치 완료 |
| PR Revert | 몬스터 부활 — Redis `hp_before_defeat` 값 복원 |


### 2-4. 알림 주기
1시간마다 프로젝트 참여자 전원에게 이메일 발송. 이메일 내용: 현재 생존 중인 몬스터 목록, 각 HP, 처치까지 필요한 코멘트 수.


## 3. 시스템 아키텍처 (RabbitMQ 도입)


### 3-1. RabbitMQ란?
RabbitMQ는 서비스와 서비스 사이에서 메시지를 주고받는 것을 중계해주는 **메시지 브로커**다. 서비스 A가 서비스 B를 직접 호출하는 대신, 메시지를 큐에 넣어두면 서비스 B가 자신의 속도에 맞춰 꺼내서 처리하는 구조다. AMQP 프로토콜 기반이며, Exchange(라우터) → Queue(우편함) → Consumer(처리자) 구조로 동작한다.


### 이 프로젝트에서 RabbitMQ를 쓰는 이유

  - Grader(6시간 배치)와 Notifier(1시간 배치)의 실행 주기가 달라 REST 동기 호출로는 타이밍을 맞추기 어렵다.
  - 한 서비스가 잠깐 다운되어도 메시지가 큐에 남아 있어 유실이 없다 (durable queue).
  - 나중에 Slack 알림, 슬래시 커맨드 같은 subscriber를 추가할 때 Exchange에 바인딩만 하면 된다.


### 3-2. Exchange / Queue 구성

**Exchange: pr.events (topic)**
- `Collector → Grader` | routing key: `pr.created` — 새 PR 수집 완료 시
- `Collector → Grader` | routing key: `pr.closed` — PR 머지/클로즈 시
- `Collector → Grader` | routing key: `pr.reverted` — Revert 이벤트 시

**Exchange: monster.events (fanout)**
- `Grader → Notifier` | routing key: `monster.hp_updated` — 6시간 HP 성장 완료 시
- `Grader → Notifier` | routing key: `monster.defeated` — 처치 완료 시
- `Grader → Notifier` | routing key: `monster.revived` — Revert 부활 시

**Exchange: damage.events (direct)**
- `Collector → Grader` | routing key: `damage.applied` — 코멘트 데미지 발생 시


### 3-3. 전체 아키텍처 다이어그램

![PR Zombie Hunter 시스템 아키텍처](./docs/images/system_achitecture.png)


## 4. DB 스키마


### 4-1. ERD 구조


**project (프로젝트)**

| 컬럼 | 타입 | 키 |
| --- | --- | --- |
| id | BIGINT AUTO_INCREMENT | PK |
| name | VARCHAR(100) | |
| repository_url | VARCHAR(255) | |
| created_at | TIMESTAMP | |

**hunter (헌터)**

| 컬럼 | 타입 | 키 |
| --- | --- | --- |
| id | VARCHAR(100) | PK |
| name | VARCHAR(50) | |
| email | VARCHAR(150) | |
| github_username | VARCHAR(100) | |
| created_at | TIMESTAMP | |

**project_member (프로젝트 멤버)**

| 컬럼 | 타입 | 키 |
| --- | --- | --- |
| hunter_id | VARCHAR(100) | FK |
| project_id | BIGINT AUTO_INCREMENT | FK |

**email_verification (이메일 인증)**

| 컬럼 | 타입 | 키 |
| --- | --- | --- |
| id | BIGINT AUTO_INCREMENT | PK |
| email | VARCHAR(150) | |
| verification_code | VARCHAR(10) | |
| expires_at | TIMESTAMP | |
| is_verified | BOOLEAN | |

**monster_pr (몬스터)**

| 컬럼 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | VARCHAR(50) | PK | GitHub PR 고유 ID |
| id2 | BIGINT AUTO_INCREMENT | FK | 프로젝트 식별자 |
| title | VARCHAR(255) | | |
| author | VARCHAR(100) | | |
| current_hp | INT | | 실시간 남은 HP |
| max_hp | INT | | 시간 경과에 따라 늘어나는 Max HP |
| status | VARCHAR(20) | | ALIVE(전투중), SLAIN(처치완료) |
| updated_at | TIMESTAMP | | 1시간 간격 체력 증가 및 메일 발송 체크용 |

**email_dispatch_log (이메일 발송 이력)**

| 컬럼 | 타입 | 키 |
| --- | --- | --- |
| id | BIGINT AUTO_INCREMENT | PK |
| hunter_id | VARCHAR(100) | FK |
| monster_pr_id | VARCHAR(50) | FK |
| project_id | BIGINT AUTO_INCREMENT | FK |
| subject | VARCHAR(255) | |
| status | VARCHAR(20) | PENDING, SUCCESS, FAILED |
| sent_at | TIMESTAMP | |


※ `damage_log`는 대량 로그 데이터 특성에 맞춰 **MongoDB**에 별도 저장 (아래 섹션 참고)


## 5. 사용자 흐름 (User Flow)

```
[개발자]
    │ PR 생성 / 코멘트 작성
    ▼
[GitHub]
    │ Webhook 이벤트 발송
    ▼
[Collector :8081]
    │ PR 수집 → MySQL 저장
    │ MQ 발행 → pr.events (pr.created / damage.applied)
    ▼
[RabbitMQ]
    │ 라우팅
    ├── pr.events → [Grader :8082]
    │       │ 6시간마다 HP × 2 (가상 스레드)
    │       │ HP 연산 → Redis 캐시 갱신
    │       │ 데미지 로그 → MongoDB 저장
    │       │ MQ 발행 → monster.events (hp_updated / defeated)
    │       │ Redis: hp_before_defeat 저장
    │       ▼
    └── monster.events → [Notifier :8083]
            │ 1시간마다 전체 이메일 발송
            ▼
          [팀원 수신 — HP 현황 이메일]

[팀원]
    │ 대시보드 접속 → REST API 조회
    ▼
[프론트엔드 (React)] ← REST API :8084 (API Gateway 경유)
    │ HP 바 + 팀원별 데미지 기여 확인
    │ PR 머지 → 처치 완료
```


## 6. 기술 스택


**백엔드**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| 언어 / 프레임워크 | Java 21 · Spring Boot | MSA 3개 서비스(Collector, Grader, Notifier) 구현 |
| API | REST API (Spring MVC) | 대시보드 데이터 조회 — HP 현황, 팀원 데미지 집계 |
| API Gateway | Spring Cloud Gateway | 경로 기반 라우팅, JWT 인증, Rate Limit |
| 동시성 | Java 21 Virtual Threads | 6시간 배치 시 전체 몬스터 HP 병렬 갱신 |

**메시지 / 이벤트**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| 메시지 브로커 | RabbitMQ | 서비스 간 비동기 통신 (pr.events · monster.events · damage.events) |

**데이터 저장소**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| 관계형 DB | MySQL 8 | project · hunter · monster_pr · email_dispatch_log 등 핵심 도메인 데이터 |
| 도큐먼트 DB | MongoDB | damage_log — 코멘트 데미지 로그 (대량 append-only 데이터) |
| 캐시 / 인메모리 | Redis | ① 현재 HP 캐시 (실시간 조회 최적화) · ② hp_before_defeat 키 (Revert 부활용) · ③ HP 차감 동시성 제어 (MULTI/EXEC) |
| DB 마이그레이션 | Flyway | SQL 마이그레이션 버전 관리 |

**외부 연동**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| PR 수집 | GitHub Webhook · GitHub REST API | PR 생성/클로즈/Revert 이벤트 수신, 코멘트 이벤트 수신 |
| 인증 | GitHub OAuth 2.0 | 팀원 GitHub 계정 연동 |
| 이메일 | Gmail SMTP (JavaMail) | 1시간마다 HP 현황 이메일 발송 |

**인프라 / DevOps**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| 컨테이너 | Docker · Docker Compose | Collector / Grader / Notifier / API / RabbitMQ / Redis / MySQL / MongoDB 8개 컨테이너 |
| CI | GitHub Actions | PR 올리면 자동 TDD 테스트 실행 → 실패 시 머지 차단 |
| CD | GitHub Actions + Railway | main 브랜치 머지 → Docker 이미지 빌드 → 자동 배포 (※ 아래 비고 참고) |
| 이슈 관리 | Jira (칸반) | Epic > Story > Task > Spike 계층 구조, PR 커밋 메시지 자동 연동 |

**프론트엔드**

| 분류 | 기술 | 용도 |
| --- | --- | --- |
| 프레임워크 | React | 대시보드 UI — HP 바, 팀원 데미지 기여 시각화 |
| API 통신 | REST API | API Gateway 경유 HP 현황 / 팀원 데미지 조회 |


> ⚠️ ※ **Railway CD 비고**: Railway 무료 플랜은 배포 가능한 Docker 이미지 수에 제한이 있어, 이 프로젝트에서는 실제 배포에 활용하지 못했다. 일정 계획에는 포함되어 있으나 미적용 상태로 남아있다.


### 주요 기술 선택 이유


### REST API — GraphQL 미사용 이유
GraphQL은 클라이언트가 필요한 필드만 골라 요청할 수 있어, 여러 화면이 서로 다른 데이터 조합을 요구할 때 오버패치(필요 이상 응답)나 언더패치(데이터 부족으로 추가 요청 필요) 문제를 줄여준다. 다만 이 프로젝트는 각 엔드포인트가 반환하는 데이터 구조가 명확하게 고정되어 있어, 화면마다 요청 필드가 달라질 여지가 적다. REST API로도 충분히 깔끔하게 설계할 수 있고, 팀의 학습 비용과 구현 복잡도를 줄이는 쪽이 더 실용적이라 판단했다.


### MongoDB — damage_log 저장소
코멘트가 달릴 때마다 누가, 언제, 어느 PR에, 얼마의 데미지를 줬는지 기록이 쌓인다. 삭제도 수정도 없이 계속 추가되는 로그성 데이터다. 팀원 8명이 여러 PR에 계속 코멘트를 달면 양이 방대해지는데, MySQL은 이런 대량 로그를 저장하기엔 스키마가 딱딱하고 쓰기 성능이 상대적으로 느리다. MongoDB는 스키마가 유연하고 로그처럼 계속 추가되는 데이터에 최적화되어 있어서 damage_log에 사용한다.


### Redis — 두 가지 역할
**① HP 빠른 처리 (캐시)**

HP는 6시간마다 2배로 커지고, 코멘트가 달릴 때마다 깎이고, 대시보드에서 팀원들이 실시간으로 조회한다. 매번 MySQL까지 갔다 오면 속도가 느리다. Redis는 메모리 기반이라 MySQL보다 100배 이상 빠르게 읽고 쓸 수 있어서, 현재 HP를 Redis에 올려두고 거기서 바로 처리한다.
**② 트랜잭션 오류 방지 (동시성 제어)**

팀원 여러 명이 같은 PR에 동시에 코멘트를 달면, HP를 동시에 깎으려는 요청이 한꺼번에 들어온다. MySQL에서 이걸 처리하면 "둘 다 20,000을 읽고 둘 다 15,000으로 저장"하는 충돌이 생길 수 있다. Redis의 MULTI/EXEC 트랜잭션을 쓰면 HP 차감을 순서대로 원자적으로 처리해서 이 문제를 막는다.


## 7. 팀 구성

| 역할 | 담당자 | 담당 영역 |
| --- | --- | --- |
| 팀장 / PM (기획) | 송윤서 | 아키텍처 설계, 기획서, 도메인 모델, Jira 에픽, 발표 자료, E2E 테스트, 통합 리뷰 |
| DevOps | 송윤서, 성수연 | CI/CD 파이프라인, Docker Compose, 모니터링 |
| 백엔드 A | 조혜연 | Collector 서비스, GitHub API / Webhook 연동 |
| 백엔드 B | 김관혁 | Grader 서비스, 가상 스레드 배치, Redis 동시성 |
| 백엔드 C | 성수연 | Notifier 서비스, REST API, DevOps |
| DB 담당 | 지현 | ERD 설계, Flyway 마이그레이션, 인덱스 최적화 |
| 프론트엔드 | 최소영 | React 대시보드 UI, HP 바, 팀원 데미지 기여 시각화 |


---


## 8. 일정 계획

| 스프린트 | 기간 | 목표 |
| --- | --- | --- |
| Sprint 1 | 5/30 ~ 6/3 | 환경 세팅, RabbitMQ 설계, HP 몬스터 도메인 모델 확정 |
| Sprint 2 | 6/4 ~ 6/7 | TDD 작성 + 핵심 기능 구현 (HP 성장, 데미지, MQ 통신) |
| Sprint 3 | 6/8 ~ 6/9 | 통합 테스트, CI/CD, Revert E2E, 데모 준비 |
