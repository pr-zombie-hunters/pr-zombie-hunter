package com.zombie.notifier.domain

import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByPullRequestId(pullRequestId: String): List<Notification>
    fun existsByPullRequestIdAndGrade(pullRequestId: String, grade: String): Boolean
}
