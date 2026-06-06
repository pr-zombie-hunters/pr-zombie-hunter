package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * 테스트 대상: ZombieNotifierService
 * 테스트 크기: Small (단위 테스트)
 *
 * [Small 테스트란?]
 * - 실제 Gmail 발송 없음 → JavaMailSender를 Mock으로 대체
 * - 실제 DB 접근 없음 → NotificationRepository를 Mock으로 대체
 * - 오직 ZombieNotifierService 내부 분기 로직만 검증
 * - 실행 속도 빠름 (수 밀리초)
 *
 * [이 테스트가 검증하는 것]
 * 1. NONE 등급(3일 미만)은 이메일을 발송하지 않는다
 * 2. 수신자 이메일 목록이 비어있으면 발송하지 않는다
 * 3. 이미 발송 이력이 있는 PR+등급은 중복 발송하지 않는다
 * 4. 정상 조건이면 수신자 전원에게 발송하고 이력을 저장한다
 */
class ZombieNotifierServiceTest : DescribeSpec({

    // ── 테스트 준비 ──────────────────────────────────────────
    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockNotificationRepository = mockk<NotificationRepository>(relaxed = true)

    // 각 테스트 전에 Mock 호출 기록 초기화 (테스트 간 간섭 방지)
    beforeEach {
        clearMocks(mockMailSender, mockNotificationRepository)
    }
    // ─────────────────────────────────────────────────────────

    describe("ZombieNotifierService") {

        /**
         * [테스트 그룹 1] 등급 조건 검사
         * - NONE 등급(3일 미만)은 아직 좀비가 아니므로 이메일 발송 대상이 아님
         * - 발송 대상: SEEDLING(3일↑), ZOMBIE(7일↑), BOSS(14일↑)
         */
        context("등급 조건 검사") {

            it("NONE 등급(3일 미만)은 이메일을 발송하지 않는다") {
                // Given: 수신자가 있고 NONE 등급(2일 방치)인 PR이 주어졌을 때
                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#1",
                    prTitle = "feat/login",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/1",
                    staleDays = 2,
                    grade = "NONE",
                )

                // Then: JavaMailSender는 한 번도 호출되지 않는다
                //       (NONE은 발송 대상 등급이 아님)
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("SEEDLING 등급(3일 이상)은 이메일을 발송한다") {
                // Given: 수신자가 있고 SEEDLING 등급(4일 방치)인 PR이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#2",
                    prTitle = "fix/button",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/2",
                    staleDays = 4,
                    grade = "SEEDLING",
                )

                // Then: JavaMailSender가 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        /**
         * [테스트 그룹 2] 수신자 목록 검사
         * - 수신자 이메일이 없으면 발송 자체가 불가능
         * - 수신자가 여러 명이면 전원에게 발송해야 함
         */
        context("수신자 목록 검사") {

            it("수신자 이메일 목록이 비어있으면 이메일을 발송하지 않는다") {
                // Given: 수신자가 없고(빈 문자열) BOSS 등급 PR이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "",   // ← 수신자 없음
                )

                // When: notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#3",
                    prTitle = "feat/auth",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/3",
                    staleDays = 15,
                    grade = "BOSS",
                )

                // Then: 수신자가 없으므로 JavaMailSender가 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("수신자가 3명이면 이메일을 3번 발송한다") {
                // Given: 수신자가 3명이고 ZOMBIE 등급 PR이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "a@gmail.com,b@gmail.com,c@gmail.com",   // ← 수신자 3명
                )

                // When: notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#4",
                    prTitle = "chore/cleanup",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/4",
                    staleDays = 9,
                    grade = "ZOMBIE",
                )

                // Then: 수신자 수만큼 JavaMailSender가 3번 호출된다
                verify(exactly = 3) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        /**
         * [테스트 그룹 3] 중복 발송 방지
         * - 같은 PR + 같은 등급으로 이미 발송한 이력이 있으면 재발송하지 않음
         */
        context("중복 발송 방지") {

            it("이미 발송 이력이 있는 PR은 중복 발송하지 않는다") {
                // Given: 같은 PR + BOSS 등급으로 이미 발송한 이력이 DB에 있을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "BOSS"
                    )
                } returns true   // ← 이미 발송됨

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: 같은 PR에 다시 notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#42",
                    prTitle = "feat/auth-refactor",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42",
                    staleDays = 15,
                    grade = "BOSS",
                )

                // Then: JavaMailSender는 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("발송 성공 후 notifications 테이블에 이력을 저장한다") {
                // Given: 발송 이력이 없고 BOSS 등급 PR이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: notify()를 호출하면
                service.notify(
                    prId = "pr-zombie-hunters/repo#42",
                    prTitle = "feat/auth-refactor",
                    prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42",
                    staleDays = 15,
                    grade = "BOSS",
                )

                // Then: notificationRepository.save()가 1번 호출된다
                //       (이력을 저장해야 다음 번에 중복 발송을 막을 수 있음)
                verify(exactly = 1) { mockNotificationRepository.save(any<Notification>()) }
            }
        }
    }
})
