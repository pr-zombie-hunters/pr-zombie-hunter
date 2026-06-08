package com.zombie.graphql.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_requests")  // collector가 저장하는 테이블명
class PullRequestEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val prNumber: Int,
    val title: String,
    val author: String,
    val repoFullName: String,
    val htmlUrl: String,
    val state: String,
    val lastActivityAt: LocalDateTime,
    val zombieGrade: String = "NONE",
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
