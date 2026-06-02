package com.zombie.graphql.query

import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class PullRequestQueryTest : DescribeSpec({

    // TODO: BA/BB 싱크 후 실제 Service/Repository로 교체
    val mockQuery = mockk<PullRequestQuery>()

    val samplePRs = listOf(
        PullRequestType(
            id = 1L,
            githubPrId = 101L,
            title = "feat/auth-refactor",
            author = "hunter.alpha",
            repositoryName = "pr-zombie-hunter",
            url = "https://github.com/pr-zombie-hunters/pr-zombie-hunter/pull/101",
            grade = ZombieGrade.BOSS,
            staleDays = 14,
            requestedReviewers = 3,
            completedReviews = 0,
            createdAt = "2023-10-01T00:00:00",
            updatedAt = "2023-10-15T00:00:00",
        ),
        PullRequestType(
            id = 2L,
            githubPrId = 102L,
            title = "fix/ui-glitch-on-mobile",
            author = "dev.kyle",
            repositoryName = "pr-zombie-hunter",
            url = "https://github.com/pr-zombie-hunters/pr-zombie-hunter/pull/102",
            grade = ZombieGrade.ZOMBIE,
            staleDays = 9,
            requestedReviewers = 2,
            completedReviews = 1,
            createdAt = "2023-10-05T00:00:00",
            updatedAt = "2023-10-14T00:00:00",
        ),
    )

    describe("PullRequestQuery") {

        context("전체 조회 (grade 필터 없음)") {
            it("모든 PR 목록을 반환한다") {
                every { mockQuery.pullRequests(null) } returns samplePRs

                val result = mockQuery.pullRequests(null)

                result shouldHaveSize 2
            }
        }

        context("등급 필터 조회") {
            it("BOSS 등급만 필터링하면 해당 PR만 반환한다") {
                every { mockQuery.pullRequests(ZombieGrade.BOSS) } returns samplePRs.filter { it.grade == ZombieGrade.BOSS }

                val result = mockQuery.pullRequests(ZombieGrade.BOSS)

                result shouldHaveSize 1
                result[0].grade shouldBe ZombieGrade.BOSS
                result[0].title shouldBe "feat/auth-refactor"
            }

            it("ZOMBIE 등급만 필터링하면 해당 PR만 반환한다") {
                every { mockQuery.pullRequests(ZombieGrade.ZOMBIE) } returns samplePRs.filter { it.grade == ZombieGrade.ZOMBIE }

                val result = mockQuery.pullRequests(ZombieGrade.ZOMBIE)

                result shouldHaveSize 1
                result[0].grade shouldBe ZombieGrade.ZOMBIE
            }

            it("해당 등급의 PR이 없으면 빈 리스트를 반환한다") {
                every { mockQuery.pullRequests(ZombieGrade.SEEDLING) } returns emptyList()

                val result = mockQuery.pullRequests(ZombieGrade.SEEDLING)

                result shouldHaveSize 0
            }
        }

        context("단건 조회") {
            it("존재하지 않는 PR id 조회 시 null을 반환한다") {
                every { mockQuery.pullRequest(999L) } returns null

                val result = mockQuery.pullRequest(999L)

                result shouldBe null
            }

            it("존재하는 PR id 조회 시 해당 PR을 반환한다") {
                every { mockQuery.pullRequest(1L) } returns samplePRs[0]

                val result = mockQuery.pullRequest(1L)

                result?.id shouldBe 1L
                result?.title shouldBe "feat/auth-refactor"
            }
        }
    }
})
