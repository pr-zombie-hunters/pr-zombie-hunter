package com.zombie.notifier.messaging

/**
 * grader가 RabbitMQ로 보내는 몬스터 이벤트 메시지 형식
 *
 * monster.events exchange로 발행되는 메시지:
 * - eventType: "hp_updated" | "defeated" | "revived"
 * - prId: GitHub PR 식별자
 * - prTitle: PR 제목
 * - prUrl: PR 링크
 * - currentHp: 현재 HP
 * - maxHp: 최대 HP (6시간마다 2배 성장)
 * - requiredComments: 처치까지 필요한 코멘트 수 (currentHp / 5000)
 */
data class MonsterEvent(
    val eventType: String,   // hp_updated, defeated, revived
    val prId: String,
    val prTitle: String,
    val prUrl: String,
    val currentHp: Int,
    val maxHp: Int,
    val requiredComments: Int = (currentHp + 4999) / 5000, // 올림 계산
)
