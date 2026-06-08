package com.zombie.collector

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_request")   // api-service와 공유 테이블
class PullRequest(

    @Id
    @Column(length = 100)
    val id: String,              // "repoFullName#prNumber" 형식 (예: pr-zombie-hunters/repo#21)

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val author: String,

    @Column(nullable = false)
    var lastActivityAt: LocalDateTime,

    @Column(nullable = false)
    var zombieGrade: String = "NONE",
)