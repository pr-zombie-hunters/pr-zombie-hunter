package com.zombie.graphql.mutation

import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import com.zombie.graphql.entity.PullRequestJpaRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
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
                // 중복 처치 기록 없음 → 정상 저장 흐름
                every { mockHunterActionRepository.existsByPrIdAndActionType("pr-zombie-hunters/repo#42", "HUNT") } returns false
                every { mockHunterActionRepository.save(any()) } returns savedAction

                val result = mutation.markAsHunted("pr-zombie-hunters/repo#42", "홍길동")

                result.prId shouldBe "pr-zombie-hunters/repo#42"
                result.hunterId shouldBe "홍길동"
                result.actionType shouldBe "HUNT"
                verify(exactly = 1) { mockHunterActionRepository.save(any()) }
            }
        }

        // [TDD - Red] 이미 처치완료된 PR Mutation 재실행
        // 동일한 PR에 HUNT 액션이 이미 존재할 경우, 중복 처치를 방지해야 한다.
        // 현재 HunterMutation 구현체는 중복 체크를 하지 않으므로 이 테스트는 실패(Red) 상태이다.
        // → HunterMutation.markAsHunted 내부에 existsByPrIdAndActionType 체크 후 예외를 던지도록 구현해야 Green이 된다.
        context("이미 처치완료된 PR Mutation 재실행") {
            it("이미 처치완료된 PR에 markAsHunted를 재실행하면 예외가 발생하고 저장은 이루어지지 않는다") {
                // 해당 PR에 이미 HUNT 액션이 존재하는 상황을 가정
                every { mockHunterActionRepository.existsByPrIdAndActionType("pr-zombie-hunters/repo#42", "HUNT") } returns true

                // 중복 처치 시도 → IllegalStateException 이 발생해야 한다
                shouldThrow<IllegalStateException> {
                    mutation.markAsHunted("pr-zombie-hunters/repo#42", "홍길동")
                }

                // 예외가 발생했으므로 save는 단 한 번도 호출되어서는 안 된다
                verify(exactly = 0) { mockHunterActionRepository.save(any()) }
            }
        }
    }
})
