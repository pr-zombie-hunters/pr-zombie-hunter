package com.zombie.collector

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookPayload(
    val action: String,
    @JsonProperty("pull_request") val pullRequest: PullRequestPayload,
    val repository: RepositoryPayload,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequestPayload(
    val number: Int,
    val title: String,
    @JsonProperty("html_url") val htmlUrl: String,
    @JsonProperty("updated_at") val updatedAt: String,
    val user: UserPayload,
    val state: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserPayload(
    val login: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RepositoryPayload(
    @JsonProperty("full_name") val fullName: String,
)

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val collectorService: CollectorService,
    @Value("\${WEBHOOK_SECRET}") private val webhookSecret: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @PostMapping("/github")
    fun handleGithubWebhook(
        @RequestHeader("X-GitHub-Event") event: String,
        @RequestHeader("X-Hub-Signature-256") signature: String,
        @RequestBody rawBody: String
    ): ResponseEntity<String> {

        // Secret 검증
        if (!verifySignature(rawBody, signature)) {
            log.warn("Webhook Secret 검증 실패 — 요청 거부")
            return ResponseEntity.badRequest().body("서명 검증 실패")
        }

        // PR 이벤트만 처리
        if (event != "pull_request") {
            return ResponseEntity.ok("이벤트 무시: $event")
        }

        val payload = mapper.readValue(rawBody, WebhookPayload::class.java)
        collectorService.handleWebhookEvent(payload)
        return ResponseEntity.ok("처리 완료")
    }

    private fun verifySignature(body: String, signature: String): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(webhookSecret.toByteArray(), "HmacSHA256"))
            val hash = mac.doFinal(body.toByteArray())
            val expected = "sha256=" + hash.joinToString("") { "%02x".format(it) }
            expected == signature
        } catch (e: Exception) {
            log.error("서명 검증 중 오류 발생", e)
            false
        }
    }
}