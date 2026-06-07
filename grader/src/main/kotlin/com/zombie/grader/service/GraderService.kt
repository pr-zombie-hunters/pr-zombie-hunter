package com.zombie.grader.service

import com.zombie.grader.messaging.MonsterEvent
import com.zombie.grader.messaging.MonsterEventPublisher
import org.springframework.stereotype.Service

enum class PrStatus { OPEN, MERGED, CLOSED, REVERTED }

data class ZombieMonster(
    val prId: String,
    val prTitle: String = "",
    val prUrl: String = "",
    var currentHp: Long = 10000L,
    var maxHp: Long = 10000L,
    var isDefeated: Boolean = false,
    var prStatus: PrStatus = PrStatus.OPEN,
)

data class GraderResult(val updatedHp: Long, val isDefeated: Boolean)

// Redis 연동을 위한 인터페이스 규격
interface MonsterRedisRepository {
    fun saveCurrentHp(prId: String, hp: Long)
    fun saveHpBeforeDefeat(prId: String, hp: Long)
    fun getHpBeforeDefeat(prId: String): Long?
}

@Service
class GraderService(
    private val redisRepository: MonsterRedisRepository,
    private val eventPublisher: MonsterEventPublisher,
) {
    private val DAMAGE_PER_COMMENT = 5000L

    // HP 2배 성장 — 6시간 스케줄러가 호출
    fun growMonster(monster: ZombieMonster): GraderResult {
        if (monster.isDefeated || monster.prStatus != PrStatus.OPEN) {
            return GraderResult(monster.currentHp, monster.isDefeated)
        }

        monster.currentHp *= 2
        monster.maxHp = monster.currentHp
        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)

        // notifier에게 HP 성장 이벤트 발행
        eventPublisher.publish(
            MonsterEvent(
                eventType = "hp_updated",
                prId = monster.prId,
                prTitle = monster.prTitle,
                prUrl = monster.prUrl,
                currentHp = monster.currentHp.toInt(),
                maxHp = monster.maxHp.toInt(),
            )
        )

        return GraderResult(monster.currentHp, monster.isDefeated)
    }

    // 댓글 데미지 적용 — collector 이벤트 수신 시 호출
    fun applyDamage(monster: ZombieMonster, isUniqueComment: Boolean): GraderResult {
        if (monster.isDefeated || !isUniqueComment) {
            return GraderResult(monster.currentHp, monster.isDefeated)
        }

        monster.currentHp -= DAMAGE_PER_COMMENT

        if (monster.currentHp <= 0) {
            redisRepository.saveHpBeforeDefeat(monster.prId, monster.currentHp + DAMAGE_PER_COMMENT)
            monster.currentHp = 0
            monster.isDefeated = true

            // notifier에게 처치 완료 이벤트 발행
            eventPublisher.publish(
                MonsterEvent(
                    eventType = "defeated",
                    prId = monster.prId,
                    prTitle = monster.prTitle,
                    prUrl = monster.prUrl,
                    currentHp = 0,
                    maxHp = monster.maxHp.toInt(),
                )
            )
        }

        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)
        return GraderResult(monster.currentHp, monster.isDefeated)
    }

    // PR 상태 변경 처리 (Revert 등)
    fun handlePrStatusChange(monster: ZombieMonster, newStatus: PrStatus): GraderResult {
        when (newStatus) {
            PrStatus.MERGED, PrStatus.CLOSED -> {
                redisRepository.saveHpBeforeDefeat(monster.prId, monster.currentHp)
                monster.currentHp = 0
                monster.isDefeated = true

                eventPublisher.publish(
                    MonsterEvent(
                        eventType = "defeated",
                        prId = monster.prId,
                        prTitle = monster.prTitle,
                        prUrl = monster.prUrl,
                        currentHp = 0,
                        maxHp = monster.maxHp.toInt(),
                    )
                )
            }
            PrStatus.REVERTED -> {
                val backupHp = redisRepository.getHpBeforeDefeat(monster.prId) ?: 10000L
                monster.currentHp = backupHp
                monster.isDefeated = false

                // notifier에게 부활 이벤트 발행
                eventPublisher.publish(
                    MonsterEvent(
                        eventType = "revived",
                        prId = monster.prId,
                        prTitle = monster.prTitle,
                        prUrl = monster.prUrl,
                        currentHp = monster.currentHp.toInt(),
                        maxHp = monster.maxHp.toInt(),
                    )
                )
            }
            PrStatus.OPEN -> {}
        }

        monster.prStatus = newStatus
        redisRepository.saveCurrentHp(monster.prId, monster.currentHp)
        return GraderResult(monster.currentHp, monster.isDefeated)
    }
}