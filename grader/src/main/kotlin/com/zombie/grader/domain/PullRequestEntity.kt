package com.zombie.grader.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_requests")
class PullRequestEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val githubPrId: Long,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, length = 100)
    val author: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    val repository: Repository,

    @Column(nullable = false, length = 500)
    val url: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    val updatedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val grade: ZombieGrade = ZombieGrade.NONE,

    @Column(nullable = false)
    val requestedReviewers: Int = 0,

    @Column(nullable = false)
    val completedReviews: Int = 0,

    val notifiedAt: LocalDateTime? = null,
)
