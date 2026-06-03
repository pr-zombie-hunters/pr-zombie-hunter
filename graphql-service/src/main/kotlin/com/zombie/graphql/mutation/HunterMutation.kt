package com.zombie.graphql.mutation

import com.expediagroup.graphql.server.operations.Mutation
import com.zombie.graphql.domain.HunterActionType
import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.stereotype.Component

@Component
class HunterMutation(
    private val hunterActionRepository: HunterActionJpaRepository,
    private val pullRequestRepository: PullRequestJpaRepository,
) : Mutation {

    // 처치완료 - PR 상태를 KILLED로 변경하고 hunter_actions에 기록
    fun markAsHunted(prId: Long, hunterName: String): HunterActionType {
        // PR 상태 KILLED로 변경
        pullRequestRepository.findById(prId).ifPresent { pr ->
            pr.state = "KILLED"
            pullRequestRepository.save(pr)
        }

        // 처치 기록 저장
        val action = hunterActionRepository.save(
            HunterActionEntity(
                prId = prId,
                hunterName = hunterName,
            )
        )

        return HunterActionType(
            id = action.id,
            prId = action.prId,
            hunterName = action.hunterName,
            huntedAt = action.huntedAt.toString(),
        )
    }
}
