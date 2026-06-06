package com.zombie.notifier.scheduler

import com.zombie.notifier.mail.MailService
import com.zombie.notifier.messaging.MonsterEvent
import com.zombie.notifier.messaging.MonsterHpCache
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * 테스트 대상: HourlyNotifierScheduler
 * 테스트 크기: Small (단위 테스트)
 *
 * [Small 테스트란?]
 * - 실제 스케줄러 실행 없음 (sendHourlyReport() 직접 호출)
 * - MonsterHpCache Mock으로 대체
 * - Gmail 발송 없음 (JavaMailSender Mock)
 *
 * [이 테스트가 검증하는 것]
 * 1. 살아있는 몬스터 없으면 이메일 발송 안 함
 * 2. 수신자 없으면 이메일 발송 안 함
 * 3. 몬스터가 있으면 수신자 전원에게 발송
 * 4. 이메일 본문에 HP와 처치까지 필요 코멘트 수가 포함되는지
 */
class HourlyNotifierSchedulerTest : DescribeSpec({

    val mockMailSender = mockk<JavaMailSender>(relaxed = true)
    val mockCache = mockk<MonsterHpCache>()

    val sampleMonster = MonsterEvent(
        eventType = "hp_updated",
        prId = "pr-zombie-hunters/repo#42",
        prTitle = "feat/auth-refactor",
        prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42",
        currentHp = 20000,
        maxHp = 40000,
        requiredComments = 4,
    )

    beforeEach {
        clearMocks(mockMailSender, mockCache)
    }

    describe("HourlyNotifierScheduler") {

        /**
         * [테스트 그룹 1] 발송 조건 검사
         */
        context("발송 조건 검사") {

            it("살아있는 몬스터가 없으면 이메일을 발송하지 않는다") {
                // Given: 캐시에 살아있는 몬스터가 없을 때
                every { mockCache.getAliveMonsters() } returns emptyList()

                val scheduler = HourlyNotifierScheduler(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockCache,
                    "team@gmail.com",
                )

                // When: 정기 발송 메서드를 실행하면
                scheduler.sendHourlyReport()

                // Then: JavaMailSender는 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("수신자 목록이 비어있으면 이메일을 발송하지 않는다") {
                // Given: 몬스터는 있지만 수신자가 없을 때
                every { mockCache.getAliveMonsters() } returns listOf(sampleMonster)

                val scheduler = HourlyNotifierScheduler(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockCache,
                    "",  // ← 수신자 없음
                )

                // When: 정기 발송 메서드를 실행하면
                scheduler.sendHourlyReport()

                // Then: JavaMailSender는 한 번도 호출되지 않는다
                verify(exactly = 0) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("몬스터가 있고 수신자가 있으면 이메일을 발송한다") {
                // Given: 캐시에 몬스터 1마리, 수신자 1명이 있을 때
                every { mockCache.getAliveMonsters() } returns listOf(sampleMonster)

                val scheduler = HourlyNotifierScheduler(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockCache,
                    "team@gmail.com",
                )

                // When: 정기 발송 메서드를 실행하면
                scheduler.sendHourlyReport()

                // Then: JavaMailSender가 1번 호출된다
                verify(exactly = 1) { mockMailSender.send(any<SimpleMailMessage>()) }
            }

            it("수신자가 3명이면 3번 발송한다") {
                // Given: 몬스터 1마리, 수신자 3명이 있을 때
                every { mockCache.getAliveMonsters() } returns listOf(sampleMonster)

                val scheduler = HourlyNotifierScheduler(
                    MailService(mockMailSender, "from@gmail.com"),
                    mockCache,
                    "a@gmail.com,b@gmail.com,c@gmail.com",
                )

                // When: 정기 발송 메서드를 실행하면
                scheduler.sendHourlyReport()

                // Then: 수신자 수만큼 3번 발송된다
                verify(exactly = 3) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
