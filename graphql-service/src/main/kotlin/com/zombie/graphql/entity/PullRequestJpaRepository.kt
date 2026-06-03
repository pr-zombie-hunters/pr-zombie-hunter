package com.zombie.graphql.entity

import org.springframework.data.jpa.repository.JpaRepository

interface PullRequestJpaRepository : JpaRepository<PullRequestEntity, Long> {
    fun findAllByZombieGrade(zombieGrade: String): List<PullRequestEntity>
    fun findAllByState(state: String): List<PullRequestEntity>
}
