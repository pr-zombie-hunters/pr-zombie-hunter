package com.zombie.grader.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FakeMonsterRedisRepository : MonsterRedisRepository {
    val currentHpCache = mutableMapOf<String, Long>()
    val backupHpCache = mutableMapOf<String, Long>()

    override fun saveCurrentHp(prId: String, hp: Long) { currentHpCache[prId] = hp }
    override fun saveHpBeforeDefeat(prId: String, hp: Long) { backupHpCache[prId] = hp }
    override fun getHpBeforeDefeat(prId: String): Long? = backupHpCache[prId]
}

class GraderServiceTest {
    private lateinit var fakeRedis: FakeMonsterRedisRepository
    private lateinit var graderService: GraderService

    @BeforeEach
    fun setUp() {
        fakeRedis = FakeMonsterRedisRepository()
        graderService = GraderService(fakeRedis)
    }

    @Test
    @DisplayName("[Small Test] 6시간 경과 시 HP가 2배로 성장하고 Redis에 캐싱된다")
    fun `growMonster should double HP and cache to Redis`() {
        // [Given] PR이 생성되어 초기 체력이 10,000인 상태의 몬스터가 존재합니다.
        val monster = ZombieMonster(prId = "PR-1", currentHp = 10000L)

        // [When] 6시간 주기의 성장 스케줄러가 동작하여 growMonster()가 호출됩니다.
        val result = graderService.growMonster(monster)

        // [Then] 몬스터의 반환된 HP는 2배인 20,000으로 업데이트되고, Redis 현재 상태 캐시에 정상 저장됩니다.
        assertEquals(20000L, result.updatedHp)
        assertEquals(20000L, fakeRedis.currentHpCache["PR-1"])
    }

    @Test
    @DisplayName("[Medium Test] 유니크 코멘트만 5000 데미지를 적용한다")
    fun `applyDamage reduces HP for unique comments only`() {
        // [Given] 2번의 성장을 거쳐 현재 체력이 40,000인 몬스터가 존재합니다.
        val monster = ZombieMonster(prId = "PR-2", currentHp = 40000L)

        // [When - 1단계] 동일 유저 중복 코멘트(isUniqueComment = false)로 데미지 로직이 호출됩니다.
        graderService.applyDamage(monster, isUniqueComment = false)

        // [Then - 1단계] 중복 코멘트이므로 데미지가 적용되지 않고 40,000 체력이 유지됩니다.
        assertEquals(40000L, monster.currentHp)

        // [When - 2단계] 새로운 유저 코멘트(isUniqueComment = true)로 데미지 로직이 다시 호출됩니다.
        graderService.applyDamage(monster, isUniqueComment = true)

        // [Then - 2단계] 유니크 코멘트이므로 5,000 데미지가 깎여 35,000으로 업데이트되고 Redis 캐시에 동기화됩니다.
        assertEquals(35000L, monster.currentHp)
        assertEquals(35000L, fakeRedis.currentHpCache["PR-2"])
    }

    @Test
    @DisplayName("[Large Test] PR 머지 시 처치(백업)되고, Revert 시 복원된다")
    fun `handlePrStatusChange backs up HP on MERGE and restores on REVERT`() {
        // [Given] 방치되어 체력이 80,000까지 팽창한 몬스터(PR)가 존재합니다.
        val monster = ZombieMonster(prId = "PR-3", currentHp = 80000L)
        
        // [When - 1단계] PR 상태가 MERGED로 변경되는 이벤트가 수신됩니다.
        graderService.handlePrStatusChange(monster, PrStatus.MERGED)

        // [Then - 1단계] 남은 체력(80,000)은 즉시 0이 되며 몬스터는 처치(isDefeated = true)되고, 소멸 직전 체력인 80,000이 Redis 백업 공간에 저장됩니다.
        assertEquals(0L, monster.currentHp)
        assertEquals(true, monster.isDefeated)
        assertEquals(80000L, fakeRedis.backupHpCache["PR-3"])

        // [When - 2단계] 해당 PR이 REVERTED 되는 이벤트가 수신됩니다.
        graderService.handlePrStatusChange(monster, PrStatus.REVERTED)

        // [Then - 2단계] Redis 백업 공간에서 80,000을 복원하고 몬스터는 부활(isDefeated = false)합니다.
        assertEquals(80000L, monster.currentHp)
        assertEquals(false, monster.isDefeated)
    }
}