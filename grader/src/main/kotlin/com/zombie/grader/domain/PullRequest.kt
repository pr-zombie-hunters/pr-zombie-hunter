package com.zombie.grader.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class PullRequest(
    val id: Long,
    val title: String,
    val author: String,
    val repositoryName: String,
    val url: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val staleDays: Long
        get() = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now())

    val grade: ZombieGrade
        get() = ZombieGrade.from(staleDays)
}
