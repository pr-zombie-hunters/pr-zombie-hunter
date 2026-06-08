package com.zombie.notifier.config

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * RabbitMQ 설정
 *
 * Exchange 구조 (README 아키텍처 기준):
 * - monster.events (fanout) → notifier가 구독
 *   - monster.hp_updated : 6시간마다 HP 성장 완료 시 grader가 발행
 *   - monster.defeated   : 처치 완료 시 grader가 발행
 *   - monster.revived    : Revert 부활 시 grader가 발행
 *
 * fanout 타입이라 routing key 상관없이 바인딩된 모든 큐에 메시지 전달
 */
@Configuration
class RabbitMQConfig {

    companion object {
        const val MONSTER_EVENTS_EXCHANGE = "monster.events"
        const val NOTIFIER_QUEUE = "notifier.monster.queue"
    }

    // monster.events fanout 익스체인지 생성
    @Bean
    fun monsterEventsExchange(): FanoutExchange =
        FanoutExchange(MONSTER_EVENTS_EXCHANGE, true, false)

    // notifier 전용 큐 생성 (durable: 서버 재시작해도 메시지 유지)
    @Bean
    fun notifierQueue(): Queue =
        Queue(NOTIFIER_QUEUE, true)

    // 익스체인지 ↔ 큐 바인딩
    @Bean
    fun notifierBinding(
        notifierQueue: Queue,
        monsterEventsExchange: FanoutExchange,
    ): Binding = BindingBuilder
        .bind(notifierQueue)
        .to(monsterEventsExchange)
}
