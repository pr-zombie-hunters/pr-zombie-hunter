package com.zombie.notifier.mail

import com.zombie.notifier.messaging.MonsterEvent

/**
 * HP 몬스터 시스템 기반 이메일 템플릿
 *
 * 이벤트 타입별 이메일 내용:
 * - hp_updated : 6시간마다 HP 성장 현황 알림
 * - defeated   : 몬스터 처치 완료 알림
 * - revived    : Revert로 몬스터 부활 알림
 */
object ZombieMailTemplate {

    fun subject(event: MonsterEvent): String = when (event.eventType) {
        "hp_updated" -> "[🧟 좀비 PR] ${event.prTitle} — HP ${event.currentHp} (코멘트 ${event.requiredComments}개 필요)"
        "defeated"   -> "[🎉 처치 완료] ${event.prTitle} — 팀워크로 처치했습니다!"
        "revived"    -> "[💀 몬스터 부활!] ${event.prTitle} — Revert로 좀비가 되살아났습니다"
        else         -> "[PR 알림] ${event.prTitle}"
    }

    fun body(event: MonsterEvent): String = when (event.eventType) {
        "hp_updated" -> """
            |[🧟 좀비 PR 현황]
            |
            |방치된 PR이 몬스터로 성장하고 있습니다!
            |
            |• PR: ${event.prTitle}
            |• 링크: ${event.prUrl}
            |• 현재 HP: ${event.currentHp} / ${event.maxHp}
            |• 처치까지 필요한 코멘트: ${event.requiredComments}개
            |
            |지금 코멘트를 달아 팀원들과 함께 처치하세요!
            |6시간마다 HP가 2배로 증가합니다. ⚠️
        """.trimMargin()

        "defeated" -> """
            |[🎉 몬스터 처치 완료!]
            |
            |팀원들의 협력으로 좀비 PR을 처치했습니다!
            |
            |• PR: ${event.prTitle}
            |• 링크: ${event.prUrl}
            |
            |수고하셨습니다! 다음 좀비도 함께 사냥해요 🏹
        """.trimMargin()

        "revived" -> """
            |[💀 긴급! 몬스터 부활]
            |
            |Revert로 인해 처치했던 PR이 다시 살아났습니다!
            |
            |• PR: ${event.prTitle}
            |• 링크: ${event.prUrl}
            |• 부활 HP: ${event.currentHp}
            |• 처치까지 필요한 코멘트: ${event.requiredComments}개
            |
            |즉시 대응이 필요합니다! ⚡
        """.trimMargin()

        else -> "PR ${event.prTitle} 알림"
    }
}
