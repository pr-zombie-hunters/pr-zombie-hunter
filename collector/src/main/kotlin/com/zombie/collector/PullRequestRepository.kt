package com.zombie.collector

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PullRequestRepository : JpaRepository<PullRequest, String> {
    // id = "repoFullName#prNumber" 형식이라 findById로 중복 확인
}