package com.zombie.graphql.mutation

import com.zombie.graphql.domain.HunterActionType
import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class HunterMutation(
    private val hunterActionRepository: HunterActionJpaRepository,
    private val pullRequestRepository: PullRequestJpaRepository,
) {
    @MutationMapping
    fun markAsHunted(@Argument prId: String, @Argument hunterId: String, @Argument actionType: String = "HUNT"): HunterActionType {
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
