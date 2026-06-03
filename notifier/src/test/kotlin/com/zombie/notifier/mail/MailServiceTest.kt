package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class MailServiceTest : DescribeSpec({

    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockNotificationRepository = mockk<NotificationRepository>()
    val mailService = MailService(mockMailSender, "test@gmail.com")

    describe("MailService") {

        context("등급별 알림 발송") {
            it("ZOMBIE 등급 PR은 이메일이 발송된다") {
                mailService.sendZombieAlert(
                    to = "dev@example.com",
                    subject = "[ZOMBIE] fix/ui-glitch 방치 중",
                    body = "7일 이상 방치된 PR입니다.",
                )

                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("BOSS 등급 PR은 이메일이 발송된다") {
                mailService.sendZombieAlert(
                    to = "dev@example.com",
                    subject = "[BOSS] feat/auth-refactor 치명적 방치",
                    body = "14일 이상 방치된 보스 좀비 PR입니다.",
                )

                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        context("중복 발송 차단") {
            it("이미 같은 등급으로 알림을 보낸 PR은 다시 발송하지 않는다") {
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(1L, "ZOMBIE")
                } returns true

                val alreadySent = mockNotificationRepository
                    .existsByPullRequestIdAndGrade(1L, "ZOMBIE")

                alreadySent shouldBe true
                // 이미 발송됐으면 mailSender 호출 안 함
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("아직 알림을 보내지 않은 PR은 발송된다") {
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(2L, "BOSS")
                } returns false
                every {
                    mockNotificationRepository.save(any<Notification>())
                } returns mockk()

                val alreadySent = mockNotificationRepository
                    .existsByPullRequestIdAndGrade(2L, "BOSS")

                alreadySent shouldBe false

                // 발송되지 않은 PR → 이메일 발송
                mailService.sendZombieAlert(
                    to = "dev@example.com",
                    subject = "[BOSS] 보스 좀비 발견",
                    body = "즉시 처치 필요합니다.",
                )

                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
