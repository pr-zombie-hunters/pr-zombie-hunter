package com.zombie.grader.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MonsterGrowthScheduler(
    private val graderService: GraderService
) {
    // 6시간 주기 = 21600000 밀리초
    @Scheduled(fixedRate = 21600000)
    fun triggerGrowth() {
        println("[Grader Scheduler] 6시간 경과: 전체 몬스터 HP 2배 성장 로직 실행")
        // 향후 DB 연동 파트에서 살아있는 PR 목록을 가져오면 이곳에서 반복문을 돌며 graderService.
    }
}
