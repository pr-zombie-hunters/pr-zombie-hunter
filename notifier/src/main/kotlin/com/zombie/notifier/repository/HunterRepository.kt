package com.zombie.notifier.repository

import com.zombie.notifier.entity.HunterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface HunterRepository : JpaRepository<HunterEntity, String>
