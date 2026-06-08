package com.zombie.notifier.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * 테스트 대상: NotificationRepository
 * 테스트 크기: Medium (통합 테스트)
 *
 * [Medium 테스트란?]
 * - 실제 DB 연결 (H2 인메모리 DB — MySQL 대신 테스트용)
 * - Spring 전체 컨텍스트 로드 (@SpringBootTest)
 * - 실제 SQL이 실행되고 결과를 검증
 * - Small보다 느리지만 실제 DB 동작을 확인할 수 있음
 *
 * [이 테스트가 검증하는 것]
 * 1. Notification을 저장하면 실제로 DB에 저장되는지
 * 2. existsByPullRequestIdAndGrade()가 정확히 동작하는지
 * 3. 다른 PR이나 다른 이벤트 타입은 구분되는지
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest : DescribeSpec() {

    // 람다 안이 아닌 클래스 프로퍼티로 선언해야 @Autowired 동작
    @Autowired
    lateinit var notificationRepository: NotificationRepository

    init {
        extensions(SpringExtension)

        describe("NotificationRepository") {

            /**
             * [테스트 그룹 1] 저장 및 조회
             */
            context("저장 및 조회") {

                it("Notification을 저장하면 DB에 정상적으로 저장된다") {
                    // Given: 저장할 Notification 객체가 주어졌을 때
                    val notification = Notification(
                        pullRequestId = "pr-zombie-hunters/repo#42",
                        recipientEmail = "team@gmail.com",
                        grade = "hp_updated",
                    )

                    // When: save()를 호출하면
                    val saved = notificationRepository.save(notification)

                    // Then: id가 부여되고 값이 정확히 저장된다
                    saved.id shouldBe 1L
                    saved.pullRequestId shouldBe "pr-zombie-hunters/repo#42"
                    saved.grade shouldBe "hp_updated"
                }
            }

            /**
             * [테스트 그룹 2] 중복 발송 방지 쿼리 검증
             */
            context("중복 발송 방지 쿼리") {

                it("저장된 PR+이벤트 타입 조합은 exists가 true를 반환한다") {
                    // Given: PR#42에 hp_updated 이벤트로 발송 이력이 저장됐을 때
                    notificationRepository.save(
                        Notification(
                            pullRequestId = "pr-zombie-hunters/repo#42",
                            recipientEmail = "team@gmail.com",
                            grade = "hp_updated",
                        )
                    )

                    // When: 같은 PR + 같은 이벤트 타입으로 exists를 조회하면
                    val result = notificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "hp_updated"
                    )

                    // Then: true가 반환된다 (이미 발송됨)
                    result shouldBe true
                }

                it("저장되지 않은 PR은 exists가 false를 반환한다") {
                    // Given: 아무 이력도 없을 때

                    // When: 없는 PR로 exists를 조회하면
                    val result = notificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#999", "hp_updated"
                    )

                    // Then: false가 반환된다 (미발송)
                    result shouldBe false
                }

                it("같은 PR이라도 다른 이벤트 타입은 별개로 인식한다") {
                    // Given: PR#42에 hp_updated 이력만 있을 때
                    notificationRepository.save(
                        Notification(
                            pullRequestId = "pr-zombie-hunters/repo#42",
                            recipientEmail = "team@gmail.com",
                            grade = "hp_updated",
                        )
                    )

                    // When: 같은 PR이지만 defeated 이벤트 타입으로 조회하면
                    val result = notificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#42", "defeated"
                    )

                    // Then: false가 반환된다 (defeated는 아직 발송 안 함)
                    result shouldBe false
                }

                it("다른 PR의 이벤트는 구분된다") {
                    // Given: PR#42에 hp_updated 이력이 있을 때
                    notificationRepository.save(
                        Notification(
                            pullRequestId = "pr-zombie-hunters/repo#42",
                            recipientEmail = "team@gmail.com",
                            grade = "hp_updated",
                        )
                    )

                    // When: 다른 PR#10으로 같은 이벤트 타입을 조회하면
                    val result = notificationRepository.existsByPullRequestIdAndGrade(
                        "pr-zombie-hunters/repo#10", "hp_updated"
                    )

                    // Then: false가 반환된다 (PR#10은 발송 이력 없음)
                    result shouldBe false
                }
            }
        }
    }
}
