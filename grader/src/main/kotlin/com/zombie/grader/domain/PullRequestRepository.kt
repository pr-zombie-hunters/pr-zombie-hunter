package com.zombie.grader.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PullRequestRepository : JpaRepository<PullRequestEntity, String> {

    // 살아있는 PR만 조회 (처치 완료 제외)
    fun findAllByZombieGradeNot(grade: String): List<PullRequestEntity>
}
