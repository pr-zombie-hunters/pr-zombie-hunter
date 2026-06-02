package com.zombie.collector

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// Webhook 페이로드 데이터 구조 정의
data class WebhookPayload(
    val action: String,
    @JsonProperty("pull_request") val pullRequest: PullRequestPayload,
    val repository: RepositoryPayload
)

data class PullRequestPayload(
    val number: Int,
    val title: String,
    @JsonProperty("html_url") val htmlUrl: String,
    @JsonProperty("updated_at") val updatedAt: String,
    val user: UserPayload,
    val state: String
)

data class UserPayload(
    val login: String
)

data class RepositoryPayload(
    @JsonProperty("full_name") val fullName: String
)

// Webhook 요청을 받는 컨트롤러
@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val collectorService: CollectorService
) {

    @PostMapping("/github")
    fun handleGithubWebhook(
        @RequestHeader("X-GitHub-Event") event: String,
        @RequestHeader("X-Hub-Signature-256") signature: String,
        @RequestBody payload: WebhookPayload
    ): ResponseEntity<String> {

        // PR 이벤트만 처리
        if (event != "pull_request") {
            return ResponseEntity.ok("이벤트 무시: $event")
        }

        collectorService.handleWebhookEvent(payload)
        return ResponseEntity.ok("처리 완료")
    }
}