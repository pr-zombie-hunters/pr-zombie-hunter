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
                every { mockRepository.findAll() } returns listOf(bossPR, zombiePR)

                val result = query.pullRequests(null)

                result shouldHaveSize 2
            }
        }

        context("등급 필터 조회") {
            it("BOSS 등급만 필터링하면 해당 PR만 반환한다") {
                every { mockRepository.findAllByZombieGrade("BOSS") } returns listOf(bossPR)

                val result = query.pullRequests(ZombieGrade.BOSS)

                result shouldHaveSize 1
                result[0].zombieGrade shouldBe ZombieGrade.BOSS
                result[0].title shouldBe "feat/auth-refactor"
            }

            it("해당 등급의 PR이 없으면 빈 리스트를 반환한다") {
                every { mockRepository.findAllByZombieGrade("SEEDLING") } returns emptyList()

                val result = query.pullRequests(ZombieGrade.SEEDLING)

                result shouldHaveSize 0
            }
        }

        context("단건 조회") {
            it("존재하지 않는 PR id 조회 시 null을 반환한다") {
                every { mockRepository.findById("없는id") } returns Optional.empty()

                val result = query.pullRequest("없는id")

                result shouldBe null
            }

            it("존재하는 PR id 조회 시 해당 PR을 반환한다") {
                every { mockRepository.findById("pr-zombie-hunters/repo#42") } returns Optional.of(bossPR)

                val result = query.pullRequest("pr-zombie-hunters/repo#42")

                result?.id shouldBe "pr-zombie-hunters/repo#42"
                result?.zombieGrade shouldBe ZombieGrade.BOSS
            }
        }
    }
})
