package com.zombie.notifier.service

import com.zombie.notifier.entity.HunterEntity
import com.zombie.notifier.repository.HunterRepository
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class EmailNotificationServiceTest : DescribeSpec({

    val mockHunterRepository = mockk<HunterRepository>()
    val mockMailSender = mockk<JavaMailSender>()
    val service = EmailNotificationService(mockHunterRepository, mockMailSender)

    describe("EmailNotificationService") {

        context("sendMonsterStatusEmail") {

            // [TDD - Red] 이메일 없는 사용자 스킵
            // 이메일이 null이거나 공백인 사용자는 발송 대상에서 제외되어야 한다.
            // 현재 구현체는 null/blank 체크 없이 전체 발송을 시도하므로 이 테스트는 실패(Red) 상태이다.
            // → EmailNotificationService.sendMonsterStatusEmail 내부에서
            //   email.isNullOrBlank() 체크 후 skip 하도록 구현해야 Green이 된다.
            it("이메일이 없는 사용자는 발송 대상에서 건너뛴다") {
                val hunters = listOf(
                    // 이메일 있음 → 발송 대상
                    HunterEntity(id = "user1", name = "홍길동", email = "hong@example.com", githubUsername = "hong"),
                    // 이메일 null → 건너뜀
                    HunterEntity(id = "user2", name = "이메일없음", email = null, githubUsername = "nomail"),
                    // 이메일 공백 → 건너뜀
                    HunterEntity(id = "user3", name = "공백이메일", email = "  ", githubUsername = "blank"),
                    // 이메일 있음 → 발송 대상
                    HunterEntity(id = "user4", name = "김철수", email = "kim@example.com", githubUsername = "kim"),
                )
                every { mockHunterRepository.findAll() } returns hunters
                every { mockMailSender.send(any<SimpleMailMessage>()) } just runs

                service.sendMonsterStatusEmail()

                // 유효한 이메일을 가진 user1, user4 에게만 발송 → 정확히 2번 호출
                verify(exactly = 2) { mockMailSender.send(any<SimpleMailMessage>()) }
            }
        }
    }
})
