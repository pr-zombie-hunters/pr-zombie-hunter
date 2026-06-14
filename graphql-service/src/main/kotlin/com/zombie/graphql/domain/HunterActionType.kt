package com.zombie.graphql.domain

// TODO: BA/BB 싱크 - hunter_action 테이블 필드명 확인 필요
data class HunterActionType(
    val id: Long,
    val prId: Long,
    val hunterName: String,
    val huntedAt: String,
)
