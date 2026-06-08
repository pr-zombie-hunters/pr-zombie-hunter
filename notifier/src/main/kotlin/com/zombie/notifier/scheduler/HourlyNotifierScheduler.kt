package com.zombie.notifier.scheduler

import com.zombie.notifier.mail.MailService
import com.zombie.notifier.messaging.MonsterHpCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 1시간마다 살아있는 몬스터 HP 현황을 팀원 전원에게 이메일 발송
 *
 * 발송 조건:
 * - 살아있는 몬스터(hp_updated 캐시에 있는 PR)가 1개 이상일 때만 발송
 * - 몬스터 없으면 이메일 발송 안 함
 *
 * 이메일 내용:
 * - 살아있는 몬스터 목록
 * - 각 PR의 현재 HP / 최대 HP
 * - 처치까지 필요한 코멘트 수 (currentHp / 5000, 올림)
 * - PR 링크
 */
@Component
class HourlyNotifierScheduler(
    private val mailService: MailService,
    private val monsterHpCache: MonsterHpCache,
    @Value("\${notifier.mail.recipients}") private val recipientsRaw: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val recipients: List<String>
        get() = recipientsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // 매 정각(1시간마다) 실행
    @Scheduled(cron = "0 0 * * * *")
    fun sendHourlyReport() {
        val monsters = monsterHpCache.getAliveMonsters()

        if (monsters.isEmpty()) {
            log.info("살아있는 몬스터 없음 - 정기 이메일 발송 스킵")
            return
        }

        if (recipients.isEmpty()) {
            log.warn("수신자 목록이 비어있어 발송 스킵")
            return
        }

        val subject = buildSubject(monsters.size)
        val body = buildBody(monsters)

        recipients.forEach { email ->
            mailService.sendZombieAlert(to = email, subject = subject, body = body)
        }

        log.info("정기 이메일 발송 완료 - 몬스터 수: ${monsters.size}, 수신자 수: ${recipients.size}")
    }

    private fun buildSubject(monsterCount: Int): String =
        "[🧟 PR 좀비 헌터] 현재 생존 중인 몬스터 ${monsterCount}마리 — 지금 처치하세요!"

    private fun buildBody(monsters: List<com.zombie.notifier.messaging.MonsterEvent>): String {
        val monsterList = monsters.joinToString("\n\n") { monster ->
            """
            |• PR: ${monster.prTitle}
            |  현재 HP: ${monster.currentHp} / ${monster.maxHp}
            |  처치까지 필요한 코멘트: ${monster.requiredComments}개 (코멘트 1개 = 5,000 데미지)
            |  링크: ${monster.prUrl}
            """.trimMargin()
        }

        return """
            |[🧟 PR 좀비 헌터 — 시간별 현황 보고]
            |
            |현재 ${monsters.size}마리의 좀비 PR이 생존 중입니다.
            |팀원들의 코멘트로 함께 처치해주세요!
            |
            |──────────────────────────────
            |$monsterList
            |──────────────────────────────
            |
            |⚠️ PR HP는 6시간마다 2배로 증가합니다.
            |빠른 리뷰로 PR 몬스터를 처치해주세요!
        """.trimMargin()
    }
}
