package com.zombie.graphql.entity

import org.springframework.data.jpa.repository.JpaRepository

interface HunterActionJpaRepository : JpaRepository<HunterActionEntity, Long> {
    fun existsByPrIdAndActionType(prId: String, actionType: String): Boolean
}
