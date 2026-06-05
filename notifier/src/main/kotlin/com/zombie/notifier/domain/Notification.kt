package com.zombie.notifier.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
class Notification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val pullRequestId: Long,

    @Column(nullable = false)
    val recipientEmail: String,

    // ZombieGrade 의존성 없이 문자열로 저장 (grader 모듈과 분리)
    @Column(nullable = false, length = 20)
    val grade: String,

    @Column(nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),
)
