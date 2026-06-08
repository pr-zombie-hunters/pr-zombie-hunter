package com.zombie.notifier.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
class Notification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    val pullRequestId: String,

    @Column(nullable = false)
    val recipientEmail: String,

    @Column(nullable = false, length = 20)
    val grade: String,

    @Column(nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),
)
