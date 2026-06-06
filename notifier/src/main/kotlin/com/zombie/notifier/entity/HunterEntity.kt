package com.zombie.notifier.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "hunter")
class HunterEntity(
    @Id
    val id: String,
    val name: String,
    val email: String?,
    val githubUsername: String,
)
