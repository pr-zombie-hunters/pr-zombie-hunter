package com.zombie.graphql.query

import com.expediagroup.graphql.server.operations.Query
import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class PullRequestQuery(
    private val pullRequestRepository: PullRequestJpaRepository,
) : Query {

    // 전체 조회 또는 등급 필터 조회
    fun pullRequests(zombieGrade: ZombieGrade? = null): List<PullRequestType> {
        val entities = if (zombieGrade == null) {
            pullRequestRepository.findAll()
        } else {
            pullRequestRepository.findAllByZombieGrade(zombieGrade.name)
        }
        return entities.map { it.toType() }
    }

    // 단건 조회 - 없으면 null 반환
    fun pullRequest(id: Long): PullRequestType? {
        return pullRequestRepository.findById(id).orElse(null)?.toType()
    }

    private fun com.zombie.graphql.entity.PullRequestEntity.toType() = PullRequestType(
        id = id,
        prNumber = prNumber,
        title = title,
        author = author,
        repoFullName = repoFullName,
        htmlUrl = htmlUrl,
        state = state,
        zombieGrade = ZombieGrade.valueOf(zombieGrade),
        staleDays = ChronoUnit.DAYS.between(lastActivityAt, LocalDateTime.now()),
        lastActivityAt = lastActivityAt.toString(),
        createdAt = createdAt.toString(),
    )
}
