package com.zombie.notifier.mail

import com.zombie.notifier.domain.Notification
import com.zombie.notifier.domain.NotificationRepository
import com.zombie.notifier.messaging.MonsterEvent
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * 테스트 대상: ZombieNotifierService
 * 테스트 크기: Small (단위 테스트)
 */
class ZombieNotifierServiceTest : DescribeSpec({
    // ── 테스트 준비 ──────────────────────────────────────────
    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockNotificationRepository = mockk<NotificationRepository>(relaxed = true)

    // hp_updated 이벤트 샘플 (grader가 보내는 메시지 형식)
    val sampleEvent = MonsterEvent(
        eventType = "hp_updated",
        prId = "pr-zombie-hunters/repo#42",
        prTitle = "feat/auth-refactor",
        prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42",
        currentHp = 20000,
        maxHp = 40000,
        requiredComments = 4,
    )

    beforeEach {
        clearMocks(mockMailSender, mockNotificationRepository)
    }
    // ─────────────────────────────────────────────────────────

    describe("ZombieNotifierService") {

        /**
         * [테스트 그룹 1] 수신자 목록 검사
         */
        context("수신자 목록 검사") {

            it("수신자 이메일 목록이 비어있으면 이메일을 발송하지 않는다") {
                // Given: 수신자가 없는(빈 문자열) 서비스가 주어졌을 때
                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "",  // ← 수신자 없음
                )

                // When: hp_updated 이벤트로 notify()를 호출하면
                service.notify(sampleEvent)

                // Then: 수신자가 없으므로 JavaMailSender는 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("수신자가 3명이면 이메일을 3번 발송한다") {
                // Given: 수신자 3명이고 발송 이력이 없을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "a@gmail.com,b@gmail.com,c@gmail.com",  // ← 수신자 3명
                )

                // When: notify()를 호출하면
                service.notify(sampleEvent)

                // Then: 수신자 수만큼 3번 발송된다
                verify(exactly = 3) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }

        /**
         * [테스트 그룹 2] 중복 발송 방지
         * - 같은 PR + 같은 이벤트 타입으로 이미 발송한 이력이 있으면 재발송하지 않음
         */
        context("중복 발송 방지") {

            it("이미 발송 이력이 있는 PR은 중복 발송하지 않는다") {
                // Given: 같은 PR + hp_updated 이벤트로 이미 발송 이력이 있을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "hp_updated"
                    )
                } returns true  // ← 이미 발송됨

                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: 같은 이벤트로 다시 notify()를 호출하면
                service.notify(sampleEvent)

                // Then: JavaMailSender는 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("발송 성공 후 notifications 테이블에 이력을 저장한다") {
                // Given: 발송 이력이 없고 수신자가 있을 때
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
                service.notify(sampleEvent)

                // Then: 다음 번 중복 방지를 위해 notificationRepository.save()가 1번 호출된다
                verify(exactly = 1) { mockNotificationRepository.save(any<Notification>()) }
            }
        }

        /**
         * [테스트 그룹 3] 이벤트 타입별 발송
         * - defeated(처치 완료), revived(부활) 이벤트도 정상 발송되는지 검증
         */
        context("이벤트 타입별 발송") {

            it("defeated 이벤트도 팀원 전원에게 발송된다") {
                // Given: 몬스터 처치 완료 이벤트와 수신자 1명이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val defeatedEvent = sampleEvent.copy(eventType = "defeated")
                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: defeated 이벤트로 notify()를 호출하면
                service.notify(defeatedEvent)

                // Then: 처치 완료 이메일이 1번 발송된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("revived 이벤트도 팀원 전원에게 발송된다") {
                // Given: Revert로 인한 몬스터 부활 이벤트와 수신자 1명이 주어졌을 때
                every {
                    mockNotificationRepository.existsByPullRequestIdAndGrade(any(), any())
                } returns false
                every { mockNotificationRepository.save(any<Notification>()) } returns mockk()

                val revivedEvent = sampleEvent.copy(eventType = "revived")
                val service = ZombieNotifierService(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockNotificationRepository,
                    "team@gmail.com",
                )

                // When: revived 이벤트로 notify()를 호출하면
                service.notify(revivedEvent)

                // Then: 부활 알림 이메일이 1번 발송된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }

})
