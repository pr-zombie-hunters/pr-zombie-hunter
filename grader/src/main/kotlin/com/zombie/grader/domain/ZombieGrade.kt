package com.zombie.grader.domain

enum class ZombieGrade {
    NONE,
    SPROUT,
    ZOMBIE,
    BOSS;

    companion object {
        fun from(staleDays: Long): ZombieGrade = when {
            staleDays >= 14 -> BOSS
            staleDays >= 7  -> ZOMBIE
            staleDays >= 3  -> SPROUT
            else            -> NONE
        }
    }
}