package com.zombie.collector

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CollectorService(
    private val pullRequestRepository: PullRequestRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleWebhookEvent(payload: WebhookPayload) {
        val repoFullName = payload.repository.fullName
        val prNumber = payload.pullRequest.number

        when (payload.action) {
            "opened", "reopened" -> {
                // 중복 확인 (TDD: 중복 PR 스킵)
                val existing = pullRequestRepository
                    .findByPrNumberAndRepoFullName(prNumber, repoFullName)

                if (existing != null) {
                    log.info("중복 PR 스킵: #$prNumber ($repoFullName)")
                    return
                }

                // 신규 PR 저장 (TDD: GitHub API 정상 응답 시 PR 수집)
                val pr = PullRequest(
                    prNumber = prNumber,
                    title = payload.pullRequest.title,
                    author = payload.pullRequest.user.login,
                    repoFullName = repoFullName,
                    htmlUrl = payload.pullRequest.htmlUrl,
                    state = "OPEN",
                    lastActivityAt = LocalDateTime.parse(
                        payload.pullRequest.updatedAt
                            .replace("Z", "")
                    )
                )
                pullRequestRepository.save(pr)
                log.info("PR 저장 완료: #$prNumber $repoFullName")
            }

            "closed" -> {
                // 처치완료 처리 (TDD: closed 상태 PR 저장)
                val existing = pullRequestRepository
                    .findByPrNumberAndRepoFullName(prNumber, repoFullName)

                if (existing != null) {
                    existing.state = "KILLED"
                    existing.lastActivityAt = LocalDateTime.now()
                    pullRequestRepository.save(existing)
                    log.info("PR 처치완료: #$prNumber ($repoFullName)")
                }
            }

            else -> log.info("처리하지 않는 액션: ${payload.action}")
        }
    }
}