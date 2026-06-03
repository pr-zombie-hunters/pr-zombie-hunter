package com.zombie.graphql.domain

// BA/BB 싱크 완료 - collector PullRequest 엔티티 기준으로 필드명 통일
data class PullRequestType(
    val id: Long,
    val prNumber: Int,
    val title: String,
    val author: String,
    val repoFullName: String,
    val htmlUrl: String,
    val state: String,
    val zombieGrade: ZombieGrade,
    val staleDays: Long,
    val lastActivityAt: String,
    val createdAt: String,
)
