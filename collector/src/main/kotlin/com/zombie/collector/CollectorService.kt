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
        val prId = "$repoFullName#$prNumber"

        when (payload.action) {
            "opened", "reopened" -> {
                if (pullRequestRepository.existsById(prId)) {
                    log.info("중복 PR 스킵: $prId")
                    return
                }

                val pr = PullRequest(
                    id = prId,
                    title = payload.pullRequest.title,
                    author = payload.pullRequest.user.login,
                    lastActivityAt = LocalDateTime.parse(
                        payload.pullRequest.updatedAt.replace("Z", "")
                    )
                )
                pullRequestRepository.save(pr)
                log.info("PR 저장 완료: $prId")
            }

            "closed" -> {
                pullRequestRepository.findById(prId).ifPresent { pr ->
                    pr.lastActivityAt = LocalDateTime.now()
                    pr.zombieGrade = "DEFEATED"
                    pullRequestRepository.save(pr)
                    log.info("PR 처치완료: $prId")
                }
            }

            else -> log.info("처리하지 않는 액션: ${payload.action}")
        }
    }

    // Revert 이벤트 처리
    fun handleRevertEvent(payload: WebhookPayload) {
        val prId = "${payload.repository.fullName}#${payload.pullRequest.number}"

        pullRequestRepository.findById(prId).ifPresent { pr ->
            pr.lastActivityAt = LocalDateTime.now()
            pr.zombieGrade = "NONE"
            pullRequestRepository.save(pr)
            log.info("PR Revert 처리 완료: $prId — 몬스터 부활 트리거")
        } ?: log.warn("Revert 수신했으나 PR 없음: $prId")
    }
}