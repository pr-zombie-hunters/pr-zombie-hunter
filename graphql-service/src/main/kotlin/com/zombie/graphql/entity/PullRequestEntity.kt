package com.zombie.graphql.entity

import jakarta.persistence.*
import java.time.LocalDateTime

// collector가 저장한 pull_requests 테이블을 graphql-service에서 읽기 전용으로 조회
@Entity
@Table(name = "pull_requests")
class PullRequestEntity(
    @Id
    val id: Long = 0,

    val prNumber: Int,
    val title: String,
    val author: String,
    val repoFullName: String,
    val htmlUrl: String,
    var state: String,
    var lastActivityAt: LocalDateTime,
    var zombieGrade: String = "NONE",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
