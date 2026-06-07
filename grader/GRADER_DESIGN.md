# 🧟‍♂️ Grader 서비스 HP 몬스터 시스템 설계

## 📌 개요
기존의 시간 기반 등급제(Enum)를 폐기하고, PR의 방치 상태를 시각화하고 재미를 더하기 위한 **HP 몬스터 시스템**과 **Redis 캐싱** 구조를 도입합니다. 
스케줄러와 사용자 인터랙션(유니크 코멘트, PR 상태 변경)에 따라 실시간으로 몬스터의 HP가 변동되며, 복구를 위한 백업 메커니즘을 지원합니다.

---

## 🎯 핵심 비즈니스 규칙

### 1. 초기 상태 설정 (Initial State)
- **[Rule 1]** PR 생성 시 기본적으로 **10,000 HP**를 가진 좀비 몬스터가 생성됩니다.
- 초기 상태: `prStatus = PrStatus.OPEN`, `isDefeated = false`.

### 2. 몬스터 성장 (Grow)
- **[Rule 2]** **6시간**마다 활성화된 몬스터(`OPEN` 상태 및 미처치 상태)의 HP가 **2배**로 증가합니다 (스케줄러 연동).
- 성장 후 최신 HP는 즉시 Redis 캐시(`saveCurrentHp`)에 동기화됩니다.

### 3. 데미지 적용 (Damage)
- **[Rule 3]** PR에 **유니크 코멘트**가 작성되면 몬스터에게 **5,000 HP**의 데미지를 줍니다.
- 중복되거나 유니크하지 않은 코멘트는 데미지를 주지 않습니다.
- 데미지 적용 후 HP는 Redis 캐시에 동기화됩니다.

### 4. 몬스터 처치 (Defeat)
- **[Rule 4]** 몬스터의 HP가 **0 이하**가 되거나, PR의 상태가 **MERGED** 또는 **CLOSED**로 변경되면 즉시 처치(`isDefeated = true`, `currentHp = 0`) 처리됩니다.
- 처치되기 직전의 HP 상태(Defeat 직전의 마지막 유효 HP)는 Redis 백업본(`hp_before_defeat`)에 저장됩니다.

### 5. 몬스터 복원 (Revert & Redis Caching)
- **[Rule 5]** PR이 **REVERTED** 상태로 변경되는 경우, Redis 백업본(`getHpBeforeDefeat`)에서 처치 직전의 HP를 읽어와 복원하고 `isDefeated = false` 상태로 되돌립니다.
- Redis에 백업본이 없는 경우 기본값인 **10,000 HP**로 복원됩니다.

---

## 💾 데이터 구조 정의

### 1. PrStatus (PR 상태)
```kotlin
enum class PrStatus { OPEN, MERGED, CLOSED, REVERTED }
```

### 2. ZombieMonster (몬스터 정보)
```kotlin
data class ZombieMonster(
    val prId: String,
    var currentHp: Long = 10000L,
    var isDefeated: Boolean = false,
    var prStatus: PrStatus = PrStatus.OPEN
)
```

### 3. MonsterRedisRepository (캐싱 인터페이스)
```kotlin
interface MonsterRedisRepository {
    fun saveCurrentHp(prId: String, hp: Long)
    fun saveHpBeforeDefeat(prId: String, hp: Long)
    fun getHpBeforeDefeat(prId: String): Long?
}
```

---

## 🧪 테스트 전략 (TDD)
- **Small Test (성장 검증)**: `growMonster`를 통해 6시간 경과 시 HP가 2배로 증가하고 Redis 캐시에 저장되는지 검증합니다.
- **Medium Test (데미지 검증)**: 유니크 코멘트 입력 시에만 5,000 HP의 데미지가 차감되는지 검증합니다.
- **Large Test (상태 변경 및 복원 검증)**: `MERGED` 또는 `CLOSED` 시 몬스터가 처치되고 HP가 백업되며, `REVERTED` 시 백업된 HP로 안전하게 복원되는지 검증합니다.