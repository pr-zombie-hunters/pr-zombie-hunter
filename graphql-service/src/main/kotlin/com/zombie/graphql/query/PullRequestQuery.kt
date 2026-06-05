package com.zombie.graphql.query

import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import com.zombie.graphql.entity.PullRequestJpaRepository
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Controller
class PullRequestQuery(
    private val pullRequestRepository: PullRequestJpaRepository,
) {
    @QueryMapping
    fun pullRequests(@Argument zombieGrade: ZombieGrade?): List<PullRequestType> {
        val entities = if (zombieGrade == null) {
            pullRequestRepository.findAll()
        } else {
            pullRequestRepository.findAllByZombieGrade(zombieGrade.name)
        }
        return entities.map { it.toType() }
    }

    @QueryMapping
    fun pullRequest(@Argument id: String): PullRequestType? {
        return pullRequestRepository.findById(id).orElse(null)?.toType()
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
