package com.zombie.grader.config

import org.springframework.amqp.core.FanoutExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Grader → Notifier RabbitMQ 발행 설정
 *
 * grader는 발행(publish)만 담당 — 큐/바인딩은 notifier가 생성
 * monster.events fanout exchange로 이벤트 발행:
 * - hp_updated : 6시간마다 HP 2배 성장 완료 시
 * - defeated   : 처치 완료(HP 0) 시
 * - revived    : Revert로 부활 시
 */
@Configuration
class RabbitMQConfig {

    companion object {
        const val MONSTER_EVENTS_EXCHANGE = "monster.events"
    }

    @Bean
    fun monsterEventsExchange(): FanoutExchange =
        FanoutExchange(MONSTER_EVENTS_EXCHANGE, true, false)
}
