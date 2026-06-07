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
    @DisplayName("[NEW-09] 6시간 경과 시 HP가 2배로 성장하고 Redis에 캐싱된다")
    fun `growMonster should double HP and cache to Redis`() {
        val monster = ZombieMonster(prId = "PR-1", currentHp = 10000L)
        
        val result = graderService.growMonster(monster)
        
        assertEquals(20000L, result.updatedHp)
        assertEquals(20000L, fakeRedis.currentHpCache["PR-1"])
    }

    @Test
    @DisplayName("[NEW-10] 중복 코멘트는 무시하고, 유니크 코멘트에만 데미지를 차감한다")
    fun `applyDamage reduces HP for unique comments only`() {
        val monster = ZombieMonster(prId = "PR-2", currentHp = 20000L)
        
        graderService.applyDamage(monster, isUniqueComment = true)
        assertEquals(15000L, monster.currentHp)

        graderService.applyDamage(monster, isUniqueComment = false)
        assertEquals(15000L, monster.currentHp)
    }

    @Test
    @DisplayName("[NEW-17] Revert 수신 시 Redis hp_before_defeat를 통해 HP를 복원한다")
    fun `handlePrStatusChange restores HP on REVERT using Redis backup`() {
        val monster = ZombieMonster(prId = "PR-3", currentHp = 40000L)
        
        graderService.handlePrStatusChange(monster, PrStatus.MERGED)
        assertEquals(0L, monster.currentHp)
        assertEquals(true, monster.isDefeated)
        assertEquals(40000L, fakeRedis.backupHpCache["PR-3"])

        graderService.handlePrStatusChange(monster, PrStatus.REVERTED)
        assertEquals(40000L, monster.currentHp)
        assertEquals(false, monster.isDefeated)
    }
}