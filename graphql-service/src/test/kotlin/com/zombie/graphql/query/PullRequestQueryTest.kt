package com.zombie.graphql.query

import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestEntity
import com.zombie.graphql.entity.PullRequestJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.Optional

/**
 * 테스트 크기: Small
 * - 외부 의존성 없음
 * - Repository를 Mock으로 대체
 * - DB 연결 불필요
 */
class PullRequestQueryTest : DescribeSpec({

    val mockRepository = mockk<PullRequestJpaRepository>()
    val query = PullRequestQuery(mockRepository)
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

    describe("PullRequestQuery") {

        context("전체 조회") {
            it("grade 필터 없으면 전체 PR을 반환한다") {
                // Given: BOSS, ZOMBIE 등급 PR 2개가 DB에 있을 때
                every { mockRepository.findAll() } returns listOf(bossPR, zombiePR)

                // When: 등급 필터 없이 전체 조회하면
                val result = query.pullRequests(null)

                // Then: 2개 모두 반환된다
                result shouldHaveSize 2
            }
        }

        context("등급 필터 조회") {
            it("BOSS 등급만 필터링하면 해당 PR만 반환한다") {
                // Given: BOSS 등급 PR이 DB에 있을 때
                every { mockRepository.findAllByZombieGrade("BOSS") } returns listOf(bossPR)

                // When: BOSS 등급으로 필터링하면
                val result = query.pullRequests(ZombieGrade.BOSS)

                // Then: BOSS PR만 1개 반환된다
                result shouldHaveSize 1
                result[0].zombieGrade shouldBe ZombieGrade.BOSS
                result[0].title shouldBe "feat/auth-refactor"
            }

            it("해당 등급의 PR이 없으면 빈 리스트를 반환한다") {
                // Given: SEEDLING 등급 PR이 없을 때
                every { mockRepository.findAllByZombieGrade("SEEDLING") } returns emptyList()

                // When: SEEDLING 등급으로 필터링하면
                val result = query.pullRequests(ZombieGrade.SEEDLING)

                // Then: 빈 리스트가 반환된다
                result shouldHaveSize 0
            }
        }

        context("단건 조회") {
            it("존재하지 않는 PR id 조회 시 null을 반환한다") {
                // Given: DB에 없는 PR id가 주어졌을 때
                every { mockRepository.findById("없는id") } returns Optional.empty()

                // When: 해당 id로 단건 조회하면
                val result = query.pullRequest("없는id")

                // Then: null이 반환된다
                result shouldBe null
            }

            it("존재하는 PR id 조회 시 해당 PR을 반환한다") {
                // Given: DB에 존재하는 BOSS 등급 PR이 있을 때
                every { mockRepository.findById("pr-zombie-hunters/repo#42") } returns Optional.of(bossPR)

                // When: 해당 id로 단건 조회하면
                val result = query.pullRequest("pr-zombie-hunters/repo#42")

                // Then: 해당 PR이 반환된다
                result?.id shouldBe "pr-zombie-hunters/repo#42"
                result?.zombieGrade shouldBe ZombieGrade.BOSS
            }
        }
    }
})
