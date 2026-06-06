package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import com.zombie.notifier.messaging.MonsterEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * 몬스터 이벤트를 받아 팀원 전원에게 이메일을 발송하는 서비스
 *
 * 발송 조건:
 * - NOTIFY_RECIPIENTS 환경변수에 등록된 팀원 전원
 * - 이미 같은 PR + 같은 이벤트로 발송한 이력이 있으면 중복 발송 안 함
 *
 * 이벤트 타입:
 * - hp_updated : HP 성장 현황 알림
 * - defeated   : 처치 완료 알림
 * - revived    : Revert 부활 알림
 */
@Service
class ZombieNotifierService(
    private val mailService: MailService,
    private val notificationRepository: NotificationRepository,
    @Value("\${notifier.mail.recipients}") private val recipientsRaw: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val recipients: List<String>
        get() = recipientsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun notify(event: MonsterEvent) {
        // 수신자가 없으면 발송 불가
        if (recipients.isEmpty()) {
            log.warn("수신자 목록이 비어있어 발송 스킵 - PR: ${event.prId}")
            return
        }

        // 중복 발송 방지 (같은 PR + 같은 이벤트 타입)
        if (notificationRepository.existsByPullRequestIdAndGrade(event.prId, event.eventType)) {
            log.info("중복 발송 방지 - PR: ${event.prId}, type: ${event.eventType}")
            return
        }

        val subject = ZombieMailTemplate.subject(event)
        val body = ZombieMailTemplate.body(event)

        // 팀원 전원에게 발송
        recipients.forEach { email ->
            mailService.sendZombieAlert(to = email, subject = subject, body = body)
            log.info("이메일 발송 완료 - PR: ${event.prId}, type: ${event.eventType}, 수신자: $email")
        }

        // 발송 이력 저장
        notificationRepository.save(
            Notification(
                pullRequestId = event.prId,
                recipientEmail = recipients.joinToString(","),
                grade = event.eventType,
            )
        )
    }
}
