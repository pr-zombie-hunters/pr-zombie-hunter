# 🧪 TDD 테스트 케이스 명세

> PR 좀비 헌터 — Collector 서비스 TDD 설계 문서  
> 담당: BA 조혜연  
> 형식: Given / When / Then

---

## SCRUM-50 | 중복 PR 입력 → 스킵 처리

**Given:** 동일한 PR(pr_number=42)이 DB에 이미 저장되어 있는 상태

**When:** 같은 pr_number=42를 가진 Webhook 이벤트를 다시 수신

**Then:** DB의 PR 레코드 수에 변화 없음 + 중복 스킵 로그 출력

---

## SCRUM-112 | GitHub API 정상 응답 시 PR 수집

**Given:** GitHub API가 PR 목록(title, number, updated_at, state, html_url)을 정상적으로 반환하는 상태

**When:** Collector가 해당 레포지토리의 PR 목록 수집 요청

**Then:** 응답받은 PR이 누락 없이 MySQL DB에 저장됨 + 저장된 PR의 last_activity_at이 updated_at과 일치

---

## SCRUM-113 | GitHub API 500 오류 처리

**Given:** GitHub API 서버가 500 Internal Server Error를 반환하는 상태

**When:** Collector가 PR 목록 수집 요청

**Then:** Collector 서버가 종료되지 않고 에러 로그 출력 + DB에 아무것도 저장되지 않음 + 사용자에게 500 응답 반환

---

## SCRUM-114 | GitHub API Rate Limit 초과 처리

**Given:** GitHub API 응답 헤더의 X-RateLimit-Remaining 값이 0인 상태 (시간당 호출 한도 초과)

**When:** Collector가 PR 목록 수집 요청

**Then:** API 호출을 중단하고 Rate Limit 경고 로그 출력 + DB에 아무것도 저장되지 않음 + 일정 시간 후 재시도

---

## SCRUM-115 | closed 상태 PR 저장

**Given:** pr_number=77인 PR이 DB에 OPEN 상태로 저장되어 있는 상태

**When:** 같은 pr_number=77의 action=closed Webhook 이벤트 수신

**Then:** DB의 해당 PR 상태가 KILLED(처치완료)로 업데이트 + 새 레코드 생성 없음

---

## SCRUM-79 | 중복 PR 스킵 테스트 완성

**Given:** 동일한 PR(pr_number=42)이 DB에 이미 존재하는 상태

**When:** 같은 pr_number=42로 Webhook 이벤트 재수신

**Then:** DB 저장 메서드(save) 호출되지 않음 + 중복 스킵 로그 출력 확인 + 테스트 BUILD SUCCESSFUL

---

## SCRUM-122 | 코멘트 → 데미지 5,000 정상 처리

**Given:** pr_id=42인 PR이 DB에 존재하고, attackerGithubId=HYcho13이 해당 PR에 처음 코멘트를 단 상태 (Redis에 `damage:pr42:userHYcho13` 키 없음)

**When:** GitHub 코멘트 Webhook 이벤트 수신 (prId=42, attackerGithubId=HYcho13, commentId=c001)

**Then:** Redis에 `damage:pr42:userHYcho13` 키 저장됨 + `damage_log` 테이블에 damageAmount=5,000으로 1건 저장됨

---

## SCRUM-123 | 중복 코멘트 → 데미지 스킵 처리

**Given:** pr_id=42인 PR에 attackerGithubId=HYcho13이 이미 코멘트를 달아 Redis에 `damage:pr42:userHYcho13` 키가 존재하는 상태

**When:** 같은 pr_id=42, attackerGithubId=HYcho13으로 코멘트 Webhook 이벤트 재수신

**Then:** `damage_log` 테이블에 추가 저장 없음 + 중복 스킵 로그 출력

