package com.zombie.graphql.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "hunter_action")
class HunterActionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val prId: String,
    val hunterId: String,
    val actionType: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
