package com.zombie.grader.domain

enum class ZombieGrade {
    NONE,
    SEEDLING,
    ZOMBIE,
    BOSS;

    companion object {
        fun from(staleDays: Long): ZombieGrade = when {
            staleDays >= 14 -> BOSS
            staleDays >= 7  -> ZOMBIE
            staleDays >= 3  -> SEEDLING
            else            -> NONE
        }
    }
}
