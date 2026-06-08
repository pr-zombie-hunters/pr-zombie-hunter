package com.zombie.graphql.domain

data class HunterActionType(
    val id: Long,
    val prId: String,
    val hunterId: String,
    val actionType: String,
    val createdAt: String,
)
