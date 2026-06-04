package com.zombie.graphql.mutation

import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import com.zombie.graphql.entity.PullRequestJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class HunterMutationTest : DescribeSpec({

    val mockHunterActionRepository = mockk<HunterActionJpaRepository>()
    val mockPullRequestRepository = mockk<PullRequestJpaRepository>()
    val mutation = HunterMutation(mockHunterActionRepository, mockPullRequestRepository)

    describe("HunterMutation") {

        context("처치완료 (markAsHunted)") {
            it("처치완료 요청 시 hunter_action이 저장되고 반환된다") {
                val savedAction = HunterActionEntity(
                    id = 1L,
                    prId = "pr-zombie-hunters/repo#42",
                    hunterId = "홍길동",
                    actionType = "HUNT",
                    createdAt = LocalDateTime.now(),
                )
                every { mockHunterActionRepository.save(any()) } returns savedAction

                val result = mutation.markAsHunted("pr-zombie-hunters/repo#42", "홍길동")

                result.prId shouldBe "pr-zombie-hunters/repo#42"
                result.hunterId shouldBe "홍길동"
                result.actionType shouldBe "HUNT"
                verify(exactly = 1) { mockHunterActionRepository.save(any()) }
            }
        }
    }
})
