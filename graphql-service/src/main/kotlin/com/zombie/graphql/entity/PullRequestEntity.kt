package com.zombie.graphql.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_request")
class PullRequestEntity(
    @Id
    val id: String,                    // VARCHAR(50) - GitHub PR ID

    val title: String,
    val author: String,
    val lastActivityAt: LocalDateTime,
    val zombieGrade: String = "NONE",
)
