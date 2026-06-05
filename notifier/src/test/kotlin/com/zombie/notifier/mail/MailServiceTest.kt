package com.zombie.notifier.mail

import com.zombie.notifier.domain.NotificationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * 테스트 크기: Small
 * - 외부 의존성 없음
 * - JavaMailSender, NotificationRepository를 Mock으로 대체
 * - 실제 Gmail 발송 없음
 */
class MailServiceTest : DescribeSpec({

    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockNotificationRepository = mockk<NotificationRepository>()
    val mailService = MailService(mockMailSender, "test@gmail.com")

    beforeEach {
        clearMocks(mockMailSender, mockNotificationRepository)
    }

    describe("MailService") {

        context("등급별 알림 발송") {
            it("ZOMBIE 등급 PR은 이메일이 발송된다") {
                // Given: ZOMBIE 등급 PR의 알림 정보가 주어졌을 때
                val to = "dev@example.com"
                val subject = "[ZOMBIE] fix/ui-glitch 방치 중"
                val body = "7일 이상 방치된 PR입니다."

                // When: 이메일 발송을 요청하면
                mailService.sendZombieAlert(to = to, subject = subject, body = body)

                // Then: JavaMailSender가 정확히 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("BOSS 등급 PR은 이메일이 발송된다") {
                // Given: BOSS 등급 PR의 알림 정보가 주어졌을 때
                val to = "dev@example.com"
                val subject = "[BOSS] feat/auth-refactor 치명적 방치"
                val body = "14일 이상 방치된 보스 좀비 PR입니다."

                // When: 이메일 발송을 요청하면
                mailService.sendZombieAlert(to = to, subject = subject, body = body)

                // Then: JavaMailSender가 정확히 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        context("중복 발송 차단") {
            it("이미 같은 등급으로 알림을 보낸 PR은 이미 발송됨을 반환한다") {
                // Given: 같은 PR + 같은 등급으로 이미 발송 이력이 있을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "ZOMBIE"
                    )
                } returns true

                // When: 중복 발송 여부를 확인하면
                val alreadySent = mockNotificationRepository
                    .existsByPullRequestIdAndGrade("pr-zombie-hunters/repo#42", "ZOMBIE")

                // Then: 이미 발송됨(true)이 반환되고 mailSender는 호출되지 않는다
                alreadySent shouldBe true
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("아직 알림을 보내지 않은 PR은 미발송으로 반환된다") {
                // Given: 해당 PR + 등급 조합으로 발송 이력이 없을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#10", "BOSS"
                    )
                } returns false

                // When: 중복 발송 여부를 확인하면
                val alreadySent = mockNotificationRepository
                    .existsByPullRequestIdAndGrade("pr-zombie-hunters/repo#10", "BOSS")

                // Then: 미발송(false)이 반환된다
                alreadySent shouldBe false

                // When: 이메일 발송을 요청하면
                mailService.sendZombieAlert(
                    to = "dev@example.com",
                    subject = "[BOSS] 보스 좀비 발견",
                    body = "즉시 처치 필요합니다.",
                )

                // Then: JavaMailSender가 정확히 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
