package com.zombie.notifier.domain

import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByPullRequestId(pullRequestId: Long): List<Notification>
    fun existsByPullRequestIdAndGrade(pullRequestId: Long, grade: String): Boolean
}
