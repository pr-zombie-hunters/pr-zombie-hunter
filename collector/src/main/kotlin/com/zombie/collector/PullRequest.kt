package com.zombie.collector

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pull_requests")
class PullRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val prNumber: Int,           // GitHub PR 번호 (중복 스킵 기준)

    @Column(nullable = false)
    val title: String,           // PR 제목 (알림 메시지에 포함)

    @Column(nullable = false)
    val author: String,          // 작성자 GitHub 닉네임

    @Column(nullable = false)
    val repoFullName: String,    // 레포 이름 (예: pr-zombie-hunters/pr-zombie-hunter)

    @Column(nullable = false)
    val htmlUrl: String,         // PR 링크 (알림에 포함)

    @Column(nullable = false)
    var state: String,           // "OPEN" 또는 "KILLED"

    @Column(nullable = false)
    var lastActivityAt: LocalDateTime,  // updated_at 기준 (방치 기간 계산 기준)

    @Column(nullable = false)
    var zombieGrade: String = "NONE",   // NONE / SPROUT / ZOMBIE / BOSS

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)