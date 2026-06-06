package com.zombie.graphql.controller

import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/pull-requests")
class PullRequestController(
    private val pullRequestRepository: PullRequestJpaRepository,
) {
    // 전체 조회 + 등급 필터
    // GET /api/pull-requests
    // GET /api/pull-requests?zombieGrade=BOSS
    @GetMapping
    fun getPullRequests(
        @RequestParam(required = false) zombieGrade: ZombieGrade?,
    ): ResponseEntity<List<PullRequestType>> {
        val entities = if (zombieGrade == null) {
            pullRequestRepository.findAll()
        } else {
            pullRequestRepository.findAllByZombieGrade(zombieGrade.name)
        }
        return ResponseEntity.ok(entities.map { it.toType() })
    }

    // 단건 조회
    // GET /api/pull-requests/{id}
    @GetMapping("/{id}")
    fun getPullRequest(@PathVariable id: String): ResponseEntity<PullRequestType> {
        val entity = pullRequestRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(entity.toType())
    }

    private fun com.zombie.graphql.entity.PullRequestEntity.toType() = PullRequestType(
        id = id,
        title = title,
        author = author,
        zombieGrade = ZombieGrade.valueOf(zombieGrade),
        staleDays = ChronoUnit.DAYS.between(lastActivityAt, LocalDateTime.now()),
        lastActivityAt = lastActivityAt.toString(),
    )
}
