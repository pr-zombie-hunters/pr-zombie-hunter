package com.zombie.graphql.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HunterActionJpaRepository : JpaRepository<HunterActionEntity, Long> {

    fun findAllByPrId(prId: String): List<HunterActionEntity>

    fun findAllByHunterId(hunterId: String): List<HunterActionEntity>

    // hunterId별 처치 수 집계 (랭킹용)
    @Query("SELECT h.hunterId, COUNT(h) FROM HunterActionEntity h GROUP BY h.hunterId ORDER BY COUNT(h) DESC")
    fun countByHunter(): List<Array<Any>>
}
