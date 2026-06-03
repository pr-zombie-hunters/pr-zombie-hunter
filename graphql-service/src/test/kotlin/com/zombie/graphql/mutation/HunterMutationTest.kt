package com.zombie.graphql.mutation

import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import com.zombie.graphql.entity.PullRequestJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.LocalDateTime
import java.util.Optional

class HunterMutationTest : DescribeSpec({

    val mockHunterActionRepository = mockk<HunterActionJpaRepository>()
    val mockPullRequestRepository = mockk<PullRequestJpaRepository>()
    val mutation = HunterMutation(mockHunterActionRepository, mockPullRequestRepository)

    describe("HunterMutation") {

        context("처치완료 (markAsHunted)") {
            it("처치완료 요청 시 PR 상태가 KILLED로 변경되고 hunter_action이 저장된다") {
                val mockPR = mockk<com.zombie.graphql.entity.PullRequestEntity>(relaxed = true)
                val savedAction = HunterActionEntity(
                    id = 1L,
                    prId = 42L,
                    hunterName = "홍길동",
                    huntedAt = LocalDateTime.now(),
                )

                every { mockPullRequestRepository.findById(42L) } returns Optional.of(mockPR)
                every { mockPullRequestRepository.save(any()) } returns mockPR
                every { mockHunterActionRepository.save(any()) } returns savedAction

                val result = mutation.markAsHunted(42L, "홍길동")

                result.prId shouldBe 42L
                result.hunterName shouldBe "홍길동"
                verify(exactly = 1) { mockHunterActionRepository.save(any()) }
                verify(exactly = 1) { mockPullRequestRepository.save(any()) }
            }

            it("존재하지 않는 PR이어도 hunter_action은 저장된다") {
                val savedAction = HunterActionEntity(
                    id = 2L,
                    prId = 999L,
                    hunterName = "김철수",
                    huntedAt = LocalDateTime.now(),
                )

                every { mockPullRequestRepository.findById(999L) } returns Optional.empty()
                every { mockHunterActionRepository.save(any()) } returns savedAction

                val result = mutation.markAsHunted(999L, "김철수")

                result.prId shouldBe 999L
                verify(exactly = 1) { mockHunterActionRepository.save(any()) }
            }
        }
    }
})
