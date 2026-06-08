package com.zombie.graphql.entity

import org.springframework.data.jpa.repository.JpaRepository

interface PullRequestJpaRepository : JpaRepository<PullRequestEntity, String> {
    fun findAllByZombieGrade(zombieGrade: String): List<PullRequestEntity>
}
