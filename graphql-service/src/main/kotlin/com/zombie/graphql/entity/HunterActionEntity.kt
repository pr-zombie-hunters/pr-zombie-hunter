package com.zombie.graphql.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "hunter_actions")
class HunterActionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val prId: Long,
    val hunterName: String,
    val huntedAt: LocalDateTime = LocalDateTime.now(),
)
