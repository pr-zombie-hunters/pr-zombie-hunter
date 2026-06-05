package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ZombieNotifierService(
    private val mailService: MailService,
    private val notificationRepository: NotificationRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // 알림 발송 대상 등급 (NONE, SEEDLING 미만은 발송 안 함 → SEEDLING부터 발송)
    private val notifiableGrades = setOf("SEEDLING", "ZOMBIE", "BOSS")

    fun notify(
        prId: String,
        prTitle: String,
        prUrl: String,
        staleDays: Long,
        grade: String,
        recipientEmail: String,
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

        // 등급별 제목/본문 생성
        val subject = ZombieMailTemplate.subject(grade, prTitle)
        val body = ZombieMailTemplate.body(grade, prTitle, prId, staleDays, prUrl)

        // 이메일 발송
        mailService.sendZombieAlert(
            to = recipientEmail,
            subject = subject,
            body = body,
        )
        log.info("이메일 발송 완료 - PR: $prId, 등급: $grade, 수신자: $recipientEmail")

        // 발송 이력 저장
        notificationRepository.save(
            Notification(
                pullRequestId = prId,
                recipientEmail = recipientEmail,
                grade = grade,
            )
        )
    }
}
