package com.zombie.grader.messaging

import com.zombie.grader.config.RabbitMQConfig
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

/**
 * monster.events fanout exchange로 이벤트 발행
 *
 * 발행 이벤트:
 * - hp_updated : HP 성장 완료
 * - defeated   : 처치 완료
 * - revived    : Revert 부활
 */
data class MonsterEvent(
    val eventType: String,   // hp_updated / defeated / revived
    val prId: String,
    val prTitle: String,
    val prUrl: String,
    val currentHp: Int,
    val maxHp: Int,
    val requiredComments: Int = (currentHp + 4999) / 5000,
)

@Component
class MonsterEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(event: MonsterEvent) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.MONSTER_EVENTS_EXCHANGE,
            "",   // fanout이라 routing key 무시
            event,
        )
        log.info("몬스터 이벤트 발행 - type: ${event.eventType}, PR: ${event.prId}, HP: ${event.currentHp}")
    }
}
