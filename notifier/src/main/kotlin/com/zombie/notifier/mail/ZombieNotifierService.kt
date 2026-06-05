package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ZombieNotifierService(
    private val mailService: MailService,
    private val notificationRepository: NotificationRepository,
    @Value("\${notifier.mail.recipients}") private val recipientsRaw: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val notifiableGrades = setOf("SEEDLING", "ZOMBIE", "BOSS")

    // 쉼표로 구분된 이메일 목록 파싱
    private val recipients: List<String>
        get() = recipientsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun notify(
        prId: String,
        prTitle: String,
        prUrl: String,
        staleDays: Long,
        grade: String,
    ) {
        // 알림 대상 등급이 아니면 스킵
        if (grade !in notifiableGrades) {
            log.info("알림 발송 스킵 (등급: $grade) - PR: $prId")
            return
        }

        // 중복 발송 방지 - 같은 PR + 같은 등급으로 이미 발송했으면 스킵
        if (notificationRepository.existsByPullRequestIdAndGrade(prId, grade)) {
            log.info("중복 발송 방지 - 이미 발송됨 (PR: $prId, 등급: $grade)")
            return
        }

        val subject = ZombieMailTemplate.subject(grade, prTitle)
        val body = ZombieMailTemplate.body(grade, prTitle, prId, staleDays, prUrl)

        // 팀원 전원에게 발송
        recipients.forEach { email ->
            mailService.sendZombieAlert(to = email, subject = subject, body = body)
            log.info("이메일 발송 완료 - PR: $prId, 등급: $grade, 수신자: $email")
        }

        // 발송 이력 저장 (대표로 첫 번째 수신자 기록)
        notificationRepository.save(
            Notification(
                pullRequestId = prId,
                recipientEmail = recipients.joinToString(","),
                grade = grade,
            )
        )
    }
}
