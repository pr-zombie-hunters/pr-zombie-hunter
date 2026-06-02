package com.zombie.graphql.mutation

import com.zombie.graphql.domain.HunterActionType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class HunterMutationTest : DescribeSpec({

    // TODO: BA/BB 싱크 후 실제 Service/Repository로 교체
    val mockMutation = mockk<HunterMutation>()

    describe("HunterMutation") {

        context("처치완료 (markAsHunted)") {
            it("처치완료 요청 시 HunterAction이 저장되고 반환된다") {
                val expected = HunterActionType(
                    id = 1L,
                    prId = 42L,
                    hunterName = "홍길동",
                    huntedAt = "2024-06-02T10:00:00",
                )
                every { mockMutation.markAsHunted(42L, "홍길동") } returns expected

                val result = mockMutation.markAsHunted(42L, "홍길동")

                result.prId shouldBe 42L
                result.hunterName shouldBe "홍길동"
                verify(exactly = 1) { mockMutation.markAsHunted(42L, "홍길동") }
            }

            it("처치완료는 정확히 한 번만 호출된다") {
                val expected = HunterActionType(
                    id = 2L,
                    prId = 10L,
                    hunterName = "김철수",
                    huntedAt = "2024-06-02T11:00:00",
                )
                every { mockMutation.markAsHunted(10L, "김철수") } returns expected

                mockMutation.markAsHunted(10L, "김철수")

                verify(exactly = 1) { mockMutation.markAsHunted(10L, "김철수") }
            }
        }
    }
})
