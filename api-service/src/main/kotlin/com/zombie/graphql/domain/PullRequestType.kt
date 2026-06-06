package com.zombie.graphql.domain

data class PullRequestType(
    val id: String,
    val title: String,
    val author: String,
    val zombieGrade: ZombieGrade,
    val staleDays: Long,
    val lastActivityAt: String,
)
