package com.zombie.grader.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class MonsterRedisRepositoryImpl(
    private val redisTemplate: StringRedisTemplate
) : MonsterRedisRepository {

    override fun saveCurrentHp(prId: String, hp: Long) {
        val key = "monster:pr:${prId}:hp"
        redisTemplate.opsForValue().set(key, hp.toString())
    }

    override fun getCurrentHp(prId: String): Long? {
        val key = "monster:pr:${prId}:hp"
        return redisTemplate.opsForValue().get(key)?.toLongOrNull()
    }

    override fun saveHpBeforeDefeat(prId: String, hp: Long) {
        val key = "monster:hp_before_defeat:${prId}"
        redisTemplate.opsForValue().set(key, hp.toString())
    }

    override fun getHpBeforeDefeat(prId: String): Long? {
        val key = "monster:hp_before_defeat:${prId}"
        return redisTemplate.opsForValue().get(key)?.toLongOrNull()
    }
}
