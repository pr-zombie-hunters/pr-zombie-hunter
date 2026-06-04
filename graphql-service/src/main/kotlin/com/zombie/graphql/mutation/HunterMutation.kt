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

    // 처치완료 - hunter_action 테이블에 기록
    fun markAsHunted(prId: String, hunterId: String, actionType: String = "HUNT"): HunterActionType {
        val action = hunterActionRepository.save(
            HunterActionEntity(
                prId = prId,
                hunterId = hunterId,
                actionType = actionType,
            )
        )
        return HunterActionType(
            id = action.id,
            prId = action.prId,
            hunterId = action.hunterId,
            actionType = action.actionType,
            createdAt = action.createdAt.toString(),
        )
    }
}
