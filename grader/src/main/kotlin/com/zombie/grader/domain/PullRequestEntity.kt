package com.zombie.grader.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_request")
class PullRequestEntity(
    @Id
    val id: String,                    // VARCHAR(50) - GitHub PR ID

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, length = 100)
    val author: String,

    @Column(nullable = false)
    var lastActivityAt: LocalDateTime,

    @Column(nullable = false, length = 20)
    var zombieGrade: String = "NONE",
)
