package com.zombie.graphql.controller

import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestEntity
import com.zombie.graphql.entity.PullRequestJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.Optional

/**
 * 테스트 크기: Small
 * - 외부 의존성 없음
 * - Repository를 Mock으로 대체
 * - DB 연결 불필요
 */
class PullRequestControllerTest : DescribeSpec({

    val mockRepository = mockk<PullRequestJpaRepository>()
    val controller = PullRequestController(mockRepository)
    val now = LocalDateTime.now()

    fun makePR(id: String, title: String, grade: String, daysAgo: Long) =
        PullRequestEntity(
            id = id,
            title = title,
            author = "tester",
            lastActivityAt = now.minusDays(daysAgo),
            zombieGrade = grade,
        )

    val bossPR = makePR("pr-zombie-hunters/repo#42", "feat/auth-refactor", "BOSS", 14)
    val zombiePR = makePR("pr-zombie-hunters/repo#10", "fix/ui-glitch", "ZOMBIE", 9)

    describe("PullRequestController") {

        context("GET /api/pull-requests - 전체 조회") {
            it("등급 필터 없으면 전체 PR을 200으로 반환한다") {
                // Given: BOSS, ZOMBIE 등급 PR 2개가 DB에 있을 때
                every { mockRepository.findAll() } returns listOf(bossPR, zombiePR)

                // When: 등급 필터 없이 전체 조회하면
                val response = controller.getPullRequests(null)

                // Then: 200 OK와 함께 2개가 반환된다
                response.statusCode shouldBe HttpStatus.OK
                response.body?.size shouldBe 2
            }
        }

        context("GET /api/pull-requests?zombieGrade=BOSS - 등급 필터 조회") {
            it("BOSS 등급 필터 시 해당 PR만 200으로 반환한다") {
                // Given: BOSS 등급 PR이 DB에 있을 때
                every { mockRepository.findAllByZombieGrade("BOSS") } returns listOf(bossPR)

                // When: BOSS 등급으로 필터링하면
                val response = controller.getPullRequests(ZombieGrade.BOSS)

                // Then: 200 OK와 함께 BOSS PR 1개만 반환된다
                response.statusCode shouldBe HttpStatus.OK
                response.body?.size shouldBe 1
                response.body?.get(0)?.zombieGrade shouldBe ZombieGrade.BOSS
            }
        }

        context("GET /api/pull-requests/{id} - 단건 조회") {
            it("존재하지 않는 id 조회 시 404를 반환한다") {
                // Given: DB에 없는 PR id가 주어졌을 때
                every { mockRepository.findById("없는id") } returns Optional.empty()

                // When: 해당 id로 단건 조회하면
                val response = controller.getPullRequest("없는id")

                // Then: 404 Not Found가 반환된다
                response.statusCode shouldBe HttpStatus.NOT_FOUND
            }

            it("존재하는 id 조회 시 해당 PR을 200으로 반환한다") {
                // Given: DB에 존재하는 BOSS 등급 PR이 있을 때
                every { mockRepository.findById("pr-zombie-hunters/repo#42") } returns Optional.of(bossPR)

                // When: 해당 id로 단건 조회하면
                val response = controller.getPullRequest("pr-zombie-hunters/repo#42")

                // Then: 200 OK와 함께 해당 PR이 반환된다
                response.statusCode shouldBe HttpStatus.OK
                response.body?.id shouldBe "pr-zombie-hunters/repo#42"
                response.body?.zombieGrade shouldBe ZombieGrade.BOSS
            }
        }
    }
})
