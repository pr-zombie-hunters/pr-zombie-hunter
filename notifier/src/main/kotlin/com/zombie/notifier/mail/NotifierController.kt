package com.zombie.notifier.mail

import org.springframework.web.bind.annotation.*

data class NotifyRequest(
    val prId: String,
    val prTitle: String,
    val prUrl: String,
    val staleDays: Long,
    val grade: String,
    val recipientEmail: String,
)

@RestController
@RequestMapping("/notify")
class NotifierController(
    private val zombieNotifierService: ZombieNotifierService,
) {
    @PostMapping
    fun notify(@RequestBody request: NotifyRequest): Map<String, String> {
        zombieNotifierService.notify(
            prId = request.prId,
            prTitle = request.prTitle,
            prUrl = request.prUrl,
            staleDays = request.staleDays,
            grade = request.grade,
            recipientEmail = request.recipientEmail,
        )
        return mapOf("status" to "ok")
    }
}
