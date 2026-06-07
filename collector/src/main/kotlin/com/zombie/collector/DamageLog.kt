package com.zombie.collector

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "damage_log")
class DamageLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val prId: Long,                  // 대상 PR

    @Column(nullable = false)
    val attackerGithubId: String,    // 코멘트 작성자 GitHub ID

    @Column(nullable = false)
    val damageAmount: Int = 5000,    // 데미지량 (고정 5,000)

    @Column(nullable = false)
    val commentId: String,           // GitHub 코멘트 ID (중복 방지)

    @Column(nullable = false)
    val attackedAt: LocalDateTime = LocalDateTime.now()
)