package com.zombie.graphql.controller

import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/pull-requests")
class PullRequestController(
    private val pullRequestRepository: PullRequestJpaRepository,
    @Value("\${github.token:}") private val githubToken: String,
) {
    private val restTemplate = RestTemplate()

    // PR 머지
    // POST /api/pull-requests/{owner}/{repo}/{number}/merge
    @PostMapping("/{owner}/{repo}/{number}/merge")
    fun mergePullRequest(
        @PathVariable owner: String,
        @PathVariable repo: String,
        @PathVariable number: Int,
    ): ResponseEntity<Map<String, String>> {
        return callGitHubApi(
            "https://api.github.com/repos/$owner/$repo/pulls/$number/merge",
            HttpMethod.PUT,
            mapOf("merge_method" to "merge")
        )
    }

    // PR 닫기
    // POST /api/pull-requests/{owner}/{repo}/{number}/close
    @PostMapping("/{owner}/{repo}/{number}/close")
    fun closePullRequest(
        @PathVariable owner: String,
        @PathVariable repo: String,
        @PathVariable number: Int,
    ): ResponseEntity<Map<String, String>> {
        return callGitHubApi(
            "https://api.github.com/repos/$owner/$repo/pulls/$number",
            HttpMethod.PATCH,
            mapOf("state" to "closed")
        )
    }

    private fun callGitHubApi(
        url: String,
        method: HttpMethod,
        body: Map<String, String>,
    ): ResponseEntity<Map<String, String>> {
        if (githubToken.isBlank()) return ResponseEntity.badRequest().body(mapOf("error" to "GitHub 토큰 없음"))
        val headers = HttpHeaders().apply {
            set("Authorization", "token $githubToken")
            set("Accept", "application/vnd.github+json")
            set("Content-Type", "application/json")
        }
        return try {
            restTemplate.exchange(url, method, HttpEntity(body, headers), String::class.java)
            ResponseEntity.ok(mapOf("status" to "ok"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "실패")))
        }
    }
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
        id = "${repoFullName}#${prNumber}",
        title = title,
        author = author,
        zombieGrade = runCatching { ZombieGrade.valueOf(zombieGrade) }.getOrDefault(ZombieGrade.NONE),
        staleDays = ChronoUnit.DAYS.between(lastActivityAt, LocalDateTime.now()),
        lastActivityAt = lastActivityAt.toString(),
    )
}
