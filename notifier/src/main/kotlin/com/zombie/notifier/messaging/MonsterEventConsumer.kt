package com.zombie.notifier.messaging

import com.zombie.notifier.config.RabbitMQConfig
import com.zombie.notifier.mail.ZombieNotifierService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * RabbitMQ monster.events 큐 구독자
 *
 * grader가 monster.events exchange에 메시지를 발행하면
 * 이 Consumer가 notifier.monster.queue에서 꺼내서 처리
 *
 * 처리 흐름:
 * grader → [monster.events exchange] → [notifier.monster.queue] → MonsterEventConsumer → ZombieNotifierService → Gmail
 */
@Component
class MonsterEventConsumer(
    private val zombieNotifierService: ZombieNotifierService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMQConfig.NOTIFIER_QUEUE])
    fun consume(event: MonsterEvent) {
        log.info("몬스터 이벤트 수신 - type: ${event.eventType}, PR: ${event.prId}")
        zombieNotifierService.notify(event)
    }
}
