package com.zombie.grader.service

enum class PrStatus { OPEN, MERGED, CLOSED, REVERTED }

data class ZombieMonster(
    val prId: String,
    var currentHp: Long = 10000L,
    var isDefeated: Boolean = false,
    var prStatus: PrStatus = PrStatus.OPEN
)

data class GraderResult(val updatedHp: Long, val isDefeated: Boolean)

interface MonsterRedisRepository {
    fun saveCurrentHp(prId: String, hp: Long)
    fun saveHpBeforeDefeat(prId: String, hp: Long)
    fun getHpBeforeDefeat(prId: String): Long?
}

class GraderService(
    private val redisRepository: MonsterRedisRepository
) {
    private val DAMAGE_PER_COMMENT = 5000L

    fun growMonster(monster: ZombieMonster): GraderResult {
        if (monster.isDefeated || monster.prStatus != PrStatus.OPEN) {
            return GraderResult(monster.currentHp, monster.isDefeated)
        }
        monster.currentHp *= 2
        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)
        return GraderResult(monster.currentHp, monster.isDefeated)
    }

    fun applyDamage(monster: ZombieMonster, isUniqueComment: Boolean): GraderResult {
        if (monster.isDefeated || !isUniqueComment) {
            return GraderResult(monster.currentHp, monster.isDefeated)
        }
        monster.currentHp -= DAMAGE_PER_COMMENT
        if (monster.currentHp <= 0) {
            redisRepository.saveHpBeforeDefeat(monster.prId, monster.currentHp + DAMAGE_PER_COMMENT)
            monster.currentHp = 0
            monster.isDefeated = true
        }
        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)
        return GraderResult(monster.currentHp, monster.isDefeated)
    }

    fun handlePrStatusChange(monster: ZombieMonster, newStatus: PrStatus): GraderResult {
        when (newStatus) {
            PrStatus.MERGED, PrStatus.CLOSED -> {
                redisRepository.saveHpBeforeDefeat(monster.prId, monster.currentHp)
                monster.currentHp = 0
                monster.isDefeated = true
            }
            PrStatus.REVERTED -> {
                val backupHp = redisRepository.getHpBeforeDefeat(monster.prId) ?: 10000L
                monster.currentHp = backupHp
                monster.isDefeated = false
            }
            PrStatus.OPEN -> {} 
        }
        monster.prStatus = newStatus
        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)
        return GraderResult(monster.currentHp, monster.isDefeated)
    }
}