package com.zombie.graphql.domain

// TODO: BA/BB 싱크 - collector/grader 팀원과 필드명 확인 필요
data class PullRequestType(
    val id: Long,
    val githubPrId: Long,
    val title: String,
    val author: String,
    val repositoryName: String,
    val url: String,
    val grade: ZombieGrade,
    val staleDays: Long,
    val requestedReviewers: Int,
    val completedReviews: Int,
    val createdAt: String,
    val updatedAt: String,
    val notifiedAt: String? = null,
)
