package com.zombie.collector

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class DamageService(
    private val redisTemplate: StringRedisTemplate,
    private val damageLogRepository: DamageLogRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleComment(prId: Long, attackerGithubId: String, commentId: String) {
        val redisKey = "damage:pr${prId}:user${attackerGithubId}"

        // Redis 중복 체크 (NEW-05)
        val alreadyDamaged = redisTemplate.hasKey(redisKey)
        if (alreadyDamaged == true) {
            log.info("중복 코멘트 스킵: pr=$prId, user=$attackerGithubId")
            return
        }

        // 처음 코멘트면 Redis에 키 저장
        redisTemplate.opsForValue().set(redisKey, "1")
        log.info("데미지 처리: pr=$prId, user=$attackerGithubId, damage=5000")

        // damage_log DB 저장 (NEW-06)
        val damageLog = DamageLog(
            prId = prId,
            attackerGithubId = attackerGithubId,
            damageAmount = 5000,
            commentId = commentId
        )
        damageLogRepository.save(damageLog)
    }
}