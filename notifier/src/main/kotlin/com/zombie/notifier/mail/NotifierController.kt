package com.zombie.notifier.mail

import com.zombie.notifier.messaging.MonsterEvent
import org.springframework.web.bind.annotation.*

data class NotifyRequest(
    val prId: String,
    val prTitle: String,
    val prUrl: String,
    val staleDays: Long,
    val grade: String,
)

@RestController
@RequestMapping("/notify")
class NotifierController(
    private val zombieNotifierService: ZombieNotifierService,
) {
    @PostMapping
    fun notify(@RequestBody request: NotifyRequest): Map<String, String> {
        zombieNotifierService.notify(
            MonsterEvent(
                eventType = request.grade,
                prId = request.prId,
                prTitle = request.prTitle,
                prUrl = request.prUrl,
                currentHp = 0,
                maxHp = 0,
            )
        )
        return mapOf("status" to "ok")
    }
}
