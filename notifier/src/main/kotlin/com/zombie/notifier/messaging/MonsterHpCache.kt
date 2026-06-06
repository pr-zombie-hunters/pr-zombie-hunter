package com.zombie.notifier.messaging

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 몬스터 HP 인메모리 캐시
 *
 * RabbitMQ로 수신한 hp_updated 이벤트를 메모리에 저장해두고
 * 1시간 스케줄러가 이 캐시를 읽어 이메일 발송에 활용
 *
 * ConcurrentHashMap 사용 이유:
 * - 여러 RabbitMQ 메시지가 동시에 들어올 수 있으므로
 *   스레드 안전한 자료구조 사용
 */
@Component
class MonsterHpCache {

    // key: prId, value: 최신 MonsterEvent
    private val cache = ConcurrentHashMap<String, MonsterEvent>()

    // hp_updated 이벤트 수신 시 캐시 갱신
    fun update(event: MonsterEvent) {
        if (event.eventType == "hp_updated") {
            cache[event.prId] = event
        }
    }

    // 처치 완료(defeated) 시 캐시에서 제거
    fun remove(prId: String) {
        cache.remove(prId)
    }

    // 현재 살아있는 모든 몬스터 목록 반환
    fun getAliveMonsters(): List<MonsterEvent> = cache.values.toList()
}
