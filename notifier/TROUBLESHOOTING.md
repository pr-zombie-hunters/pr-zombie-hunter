# 트러블슈팅 사례 (성수연)

담당 역할: Notifier 서비스 / REST API / CI 관리

---

## 1. `@DataJpaTest` → `@SpringBootTest` 호환성 문제

**상황**
`NotificationRepositoryTest`에서 `@DataJpaTest` + Kotest `DescribeSpec` 조합으로 작성했는데
Spring Boot 4.x 환경에서 생성자 주입이 동작하지 않아 테스트 실행 자체가 실패

**원인**
Spring Boot 4.x + Kotest에서 `@DataJpaTest`의 생성자 주입 방식 비호환

**해결**
`@SpringBootTest` + `@Transactional` + `@Autowired lateinit var` 조합으로 전환

```kotlin
// Before
@DataJpaTest
class NotificationRepositoryTest(
    private val notificationRepository: NotificationRepository,
) : DescribeSpec({ ... })

// After
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest : DescribeSpec({
    @Autowired
    lateinit var notificationRepository: NotificationRepository
    ...
})
```

**느낀점**
테스트 슬라이스(`@DataJpaTest`)가 항상 정답은 아님.
프레임워크 버전 조합에 따라 전체 컨텍스트(`@SpringBootTest`)가 더 안정적일 수 있음

---

## 2. Railway 무료 플랜 한계로 CD 파이프라인 제거

**상황**
GitHub Actions로 CI/CD 전체 구성 완료 후 Railway로 자동 배포까지 설정했는데
무료 플랜 이미지 수 제한에 걸려 배포 불가

**원인**
Railway 무료 플랜의 컨테이너 이미지 제한 — 5개 모듈(gateway, collector, grader, notifier, api-service) 전부 올리기엔 한계

**해결**
CD(deploy) 잡 전체 제거, 로컬 `docker compose up`으로 시연 방식 전환. CI(빌드/테스트)만 유지

```yaml
# 제거된 잡
deploy:
  needs: build
  steps:
    - name: Deploy to Railway
      ...
```

**느낀점**
무료 플랫폼 제약 조건을 아키텍처 설계 전에 먼저 파악해야 함

---

## 3. CI 모듈명 불일치로 빌드 실패 위기

**상황**
`graphql-service` → `api-service`로 모듈 리팩토링 후 실제 폴더명은 바꿨는데
`ci.yml` matrix는 `graphql-service`로 그대로 남아있어 CI가 터질 상황

**원인**
폴더 rename 시 CI 설정 파일을 동기화하지 않음

**해결**

```yaml
# Before
matrix:
  module: [ gateway, collector, grader, notifier, graphql-service ]

# After
matrix:
  module: [ gateway, collector, grader, notifier, api-service ]
```

**느낀점**
모듈명/폴더명 변경 시 CI 설정도 반드시 같이 확인해야 함. 코드만 바꾸면 끝이 아님

---

## 4. Docker 멀티모듈 빌드 — 구조 납득 안 됨

**상황**
모듈이 5개라 `docker compose up --build` 하면 Gradle 빌드를 모듈마다 따로 돌려
한 번 빌드에 10분 이상 소요. 처음엔 "왜 Dockerfile이 모듈마다 따로 있어야 하지?" 구조 자체가 납득이 안 됨

**원인**
의존성 캐시 레이어가 코드 변경 시 무효화되어 매번 Gradle 전체 재실행.
변경 없는 모듈도 전부 재빌드됨

**해결**

변경된 모듈만 선택 빌드
```bash
docker compose up --build notifier
```

Dockerfile에서 의존성 레이어를 먼저 캐싱하는 구조 활용
```dockerfile
# 의존성만 먼저 복사 → 캐시 레이어 생성
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon || true

# 코드 변경 시 여기서부터만 재실행
COPY src src
RUN gradle bootJar --no-daemon
```

**느낀점**
모듈마다 Dockerfile이 있는 건 각 서비스가 독립 배포 단위이기 때문.
마이크로서비스 구조의 핵심 개념이었고, "notifier만 따로 배포 가능"하다는 게 단점이 아닌 장점임을 이해함

---

## 5. 서비스 간 통신 부재 — RabbitMQ 도입으로 해결

**상황**
서비스 간 의사소통에 문제가 있을 수 있다는 점은 어렴풋이 느끼고 있었지만, 구체적으로 어떻게 해결해야 할지는 알지 못했다.

**원인**
마이크로서비스 구조에서 서비스 간 직접 HTTP 호출은 강결합(tight coupling)을 만들고,
한 서비스가 죽으면 연쇄 장애 발생. 비동기 이벤트 전달 수단이 없었음

**해결**
교수님 피드백으로 메시지 큐(RabbitMQ) 도입 제안을 받아 적용.

```
grader
  │
  │ monster.events (fanout exchange)
  ▼
[RabbitMQ]
  │
  ▼
MonsterEventConsumer (notifier)
  │
  ▼
이메일 발송
```

grader가 `monster.events` fanout exchange로 이벤트 발행 →
notifier가 큐를 구독해 이메일 발송하는 구조로 설계

**느낀점**
서비스 간 통신 문제는 메시지 브로커로 해결하는 게 마이크로서비스의 정석 패턴.
문제를 인지하는 것과 해결책을 아는 것은 다름 — 외부 피드백의 중요성을 느낌

---

## 6. GraphQL → REST API 전환 — 기술 선택의 적정성 판단

**상황**
초기 설계 시 GraphQL을 도입해 `api-service`를 구성했으나 실제 구현 과정에서 REST API로 전환

**원인**
GraphQL은 클라이언트가 필요한 필드만 골라 요청할 수 있어, 화면마다 요구하는 데이터 조합이 다를 때 오버패치(필요 이상 응답)·언더패치(데이터 부족으로 추가 요청 필요) 문제를 줄여준다는 장점이 있음.  
그러나 이 프로젝트는 각 엔드포인트가 반환하는 데이터 구조가 명확히 고정되어 있어, 화면마다 요청 필드가 달라질 여지가 적었음

**해결**
REST API로 전환.
- `GET /api/pull-requests` — 전체 PR 조회 (좀비 등급 필터 지원)
- `GET /api/pull-requests/{id}` — 단건 조회
- `POST /api/hunter-actions` — 처치 기록 저장
- `GET /api/hunter-actions` — 처치 이력 조회 (PR별/헌터별 필터)
- `GET /api/hunter-actions/stats` — 헌터 랭킹 집계

GraphQL 의존성 제거, Spring Web으로 교체하여 팀 학습 비용과 구현 복잡도를 줄임

**느낀점**
팀 규모에 따른 기술을 사용해야되는데
ai가 시키는대로가 아닌 그 기술의 장단점을 제대로 파악하고 사용해야겠다는 생각을 함
