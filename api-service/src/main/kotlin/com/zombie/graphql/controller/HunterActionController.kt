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
    // PR별 또는 헌터별 처치 이력 조회
    // GET /api/hunter-actions?prId=repo#42
    // GET /api/hunter-actions?hunterId=user123
    // GET /api/hunter-actions (전체)
    @GetMapping
    fun getHunterActions(
        @RequestParam(required = false) prId: String?,
        @RequestParam(required = false) hunterId: String?,
    ): ResponseEntity<List<HunterActionType>> {
        val entities = when {
            prId != null -> hunterActionRepository.findAllByPrId(prId)
            hunterId != null -> hunterActionRepository.findAllByHunterId(hunterId)
            else -> hunterActionRepository.findAll()
        }
        return ResponseEntity.ok(entities.map { it.toType() })
    }

    // 헌터별 처치 수 집계 (랭킹)
    // GET /api/hunter-actions/stats
    @GetMapping("/stats")
    fun getHunterStats(): ResponseEntity<List<Map<String, Any>>> {
        val stats = hunterActionRepository.countByHunter().map { row ->
            mapOf("hunterId" to row[0], "huntCount" to row[1])
        }
        return ResponseEntity.ok(stats)
    }

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
        return ResponseEntity.ok(action.toType())
    }

    private fun HunterActionEntity.toType() = HunterActionType(
        id = id,
        prId = prId,
        hunterId = hunterId,
        actionType = actionType,
        createdAt = createdAt.toString(),
    )
}
