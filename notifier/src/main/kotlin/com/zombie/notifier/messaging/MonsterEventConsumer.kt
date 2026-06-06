package com.zombie.notifier.messaging

import com.zombie.notifier.config.RabbitMQConfig
import com.zombie.notifier.mail.ZombieNotifierService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * RabbitMQ monster.events 큐 구독자
 *
 * 처리 흐름:
 * grader → [monster.events] → [notifier.monster.queue] → MonsterEventConsumer
 *                                                               ↓
 *                                                    MonsterHpCache 갱신 (hp_updated)
 *                                                    또는 캐시 제거 (defeated)
 *                                                               ↓
 *                                                    ZombieNotifierService → 이벤트 발생 알림 이메일
 */
@Component
class MonsterEventConsumer(
    private val zombieNotifierService: ZombieNotifierService,
    private val monsterHpCache: MonsterHpCache,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMQConfig.NOTIFIER_QUEUE])
    fun consume(event: MonsterEvent) {
        log.info("몬스터 이벤트 수신 - type: ${event.eventType}, PR: ${event.prId}")

        when (event.eventType) {
            "hp_updated" -> {
                // HP 성장 시 캐시 갱신 (1시간 스케줄러가 이 캐시를 읽어 이메일 발송)
                monsterHpCache.update(event)
            }
            "defeated" -> {
                // 처치 완료 시 캐시에서 제거 + 즉시 알림
                monsterHpCache.remove(event.prId)
                zombieNotifierService.notify(event)
            }
            "revived" -> {
                // 부활 시 캐시에 다시 추가 + 즉시 알림
                monsterHpCache.update(event.copy(eventType = "hp_updated"))
                zombieNotifierService.notify(event)
            }
        }
    }
}
