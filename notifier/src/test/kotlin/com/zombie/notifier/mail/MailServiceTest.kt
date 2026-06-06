package com.zombie.notifier.mail

import com.zombie.notifier.domain.NotificationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * 테스트 대상: MailService
 * 테스트 크기: Small (단위 테스트)
 *
 * [Small 테스트란?]
 * - 외부 시스템(Gmail, DB)에 전혀 접근하지 않음
 * - JavaMailSender → Mock으로 대체 (실제 이메일 발송 안 함)
 * - NotificationRepository → Mock으로 대체 (실제 DB 조회 안 함)
 * - 오직 MailService 내부 로직만 검증
 * - 빠르게 실행됨 (수 밀리초)
 *
 * [이 테스트가 검증하는 것]
 * 1. 이메일 발송 요청 시 JavaMailSender가 실제로 호출되는지
 * 2. 중복 발송 이력이 있을 때 발송하지 않는지
 * 3. 발송 이력이 없을 때 정상 발송하는지
 */
class MailServiceTest : DescribeSpec({

    // ── 테스트 준비 ──────────────────────────────────────────
    // Mock 객체 생성 (실제 Gmail, DB 대신 가짜 객체 사용)
    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockNotificationRepository = mockk<NotificationRepository>()

    // 테스트할 MailService 생성 (@Value 없이 직접 주입)
    val mailService = MailService(mockMailSender, "test@gmail.com")

    // 각 테스트 전에 Mock 호출 기록 초기화 (테스트 간 간섭 방지)
    beforeEach {
        clearMocks(mockMailSender, mockNotificationRepository)
    }
    // ─────────────────────────────────────────────────────────

    describe("MailService") {

        /**
         * [테스트 그룹 1] 등급별 알림 발송
         * - MailService.sendZombieAlert()가 호출되면
         *   JavaMailSender.send()가 실행되는지 검증
         */
        context("등급별 알림 발송") {

            it("ZOMBIE 등급 PR은 이메일이 발송된다") {
                // Given: ZOMBIE 등급 PR의 수신자, 제목, 본문이 준비됐을 때
                val to = "dev@example.com"
                val subject = "[ZOMBIE] fix/ui-glitch 방치 중"
                val body = "7일 이상 방치된 PR입니다."

                // When: sendZombieAlert()로 이메일 발송을 요청하면
                mailService.sendZombieAlert(to = to, subject = subject, body = body)

                // Then: JavaMailSender.send()가 정확히 1번 호출된다
                //       (실제 Gmail로 안 가도 됨 — 호출 자체가 목표)
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("BOSS 등급 PR은 이메일이 발송된다") {
                // Given: BOSS 등급 PR의 수신자, 제목, 본문이 준비됐을 때
                val to = "dev@example.com"
                val subject = "[BOSS] feat/auth-refactor 치명적 방치"
                val body = "14일 이상 방치된 보스 좀비 PR입니다."

                // When: sendZombieAlert()로 이메일 발송을 요청하면
                mailService.sendZombieAlert(to = to, subject = subject, body = body)

                // Then: JavaMailSender.send()가 정확히 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        /**
         * [테스트 그룹 2] 중복 발송 차단
         * - 같은 PR + 같은 등급으로 이미 발송한 경우 다시 보내지 않는지 검증
         */
        context("중복 발송 차단") {

            it("이미 같은 등급으로 알림을 보낸 PR은 이미 발송됨을 반환한다") {
                // Given: PR#42에 ZOMBIE 등급으로 이미 발송 이력이 DB에 있을 때
                //        → existsByPullRequestIdAndGrade()가 true를 반환하도록 Mock 설정
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "ZOMBIE"
                    )
                } returns true

                // When: 중복 발송 여부를 DB에서 조회하면
                val alreadySent = mockNotificationRepository
                    .existsByPullRequestIdAndGrade("pr-zombie-hunters/repo#42", "ZOMBIE")

                // Then: 이미 발송됨(true)이 반환된다
                //       mailSender는 0번 호출됨 (중복 발송 안 함)
                alreadySent shouldBe true
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("아직 알림을 보내지 않은 PR은 미발송으로 반환되고 이메일이 발송된다") {
                // Given: PR#10에 BOSS 등급으로 발송 이력이 없을 때
                //        → existsByPullRequestIdAndGrade()가 false를 반환하도록 Mock 설정
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#10", "BOSS"
                    )
                } returns false

                // When: 중복 발송 여부를 DB에서 조회하면
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

                // Then: JavaMailSender.send()가 정확히 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
