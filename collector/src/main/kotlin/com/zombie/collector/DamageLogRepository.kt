package com.zombie.collector

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DamageLogRepository : JpaRepository<DamageLog, Long> {
    // 특정 PR의 모든 데미지 로그 조회
    fun findByPrId(prId: Long): List<DamageLog>
}