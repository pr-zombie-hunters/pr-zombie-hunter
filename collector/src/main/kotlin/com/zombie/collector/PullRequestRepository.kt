package com.zombie.collector

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PullRequestRepository : JpaRepository<PullRequest, Long> {

    // pr_number로 이미 존재하는지 확인 (중복 스킵 기준)
    fun findByPrNumberAndRepoFullName(prNumber: Int, repoFullName: String): PullRequest?
}