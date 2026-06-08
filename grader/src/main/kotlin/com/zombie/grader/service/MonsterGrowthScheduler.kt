package com.zombie.grader.service

import com.zombie.grader.domain.PullRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 6시간마다 살아있는 PR 전체의 몬스터 HP를 2배 성장시키는 스케줄러
 *
 * 실행 흐름:
 * 1. DB에서 살아있는 PR 전체 조회 (zombieGrade != "DEFEATED")
 * 2. Redis에서 각 PR의 현재 HP 조회
 * 3. GraderService.growMonster() 호출 → HP 2배 성장 + Redis 저장
 * 4. MonsterEventPublisher → monster.events exchange로 hp_updated 이벤트 발행
 * 5. Notifier가 이벤트 수신 → MonsterHpCache 갱신 → 1시간 정기 이메일 발송
 */
@Component
class MonsterGrowthScheduler(
    private val graderService: GraderService,
    private val pullRequestRepository: PullRequestRepository,
    private val redisRepository: MonsterRedisRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 21600000) // 6시간 = 21,600,000ms
    fun triggerGrowth() {
        val alivePrs = pullRequestRepository.findAllByZombieGradeNot("DEFEATED")

        if (alivePrs.isEmpty()) {
            log.info("[Grader Scheduler] 살아있는 PR 없음 — HP 성장 스킵")
            return
        }

        log.info("[Grader Scheduler] 6시간 경과 — 살아있는 PR ${alivePrs.size}개 HP 2배 성장 시작")

        alivePrs.forEach { pr ->
            // Redis에서 현재 HP 조회 (없으면 초기값 10000)
            val currentHp = redisRepository.getCurrentHp(pr.id) ?: 10000L

            val monster = ZombieMonster(
                prId = pr.id,
                prTitle = pr.title,
                prUrl = "https://github.com/${pr.id}",
                currentHp = currentHp,
                maxHp = currentHp,
            )

            val result = graderService.growMonster(monster)
            log.info("[Grader Scheduler] PR: ${pr.id} HP 성장 완료 — ${currentHp} → ${result.updatedHp}")
        }
    }
}
