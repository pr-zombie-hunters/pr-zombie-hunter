package com.zombie.graphql.controller

import com.zombie.graphql.domain.HunterActionType
import com.zombie.graphql.entity.HunterActionEntity
import com.zombie.graphql.entity.HunterActionJpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class HunterActionRequest(
    val prId: String,
    val hunterId: String,
    val actionType: String = "HUNT",
)

@RestController
@RequestMapping("/api/hunter-actions")
class HunterActionController(
    private val hunterActionRepository: HunterActionJpaRepository,
) {
    // 처치완료
    // POST /api/hunter-actions
    @PostMapping
    fun markAsHunted(@RequestBody request: HunterActionRequest): ResponseEntity<HunterActionType> {
        val action = hunterActionRepository.save(
            HunterActionEntity(
                prId = request.prId,
                hunterId = request.hunterId,
                actionType = request.actionType,
            )
        )
        return ResponseEntity.ok(
            HunterActionType(
                id = action.id,
                prId = action.prId,
                hunterId = action.hunterId,
                actionType = action.actionType,
                createdAt = action.createdAt.toString(),
            )
        )
    }
}
