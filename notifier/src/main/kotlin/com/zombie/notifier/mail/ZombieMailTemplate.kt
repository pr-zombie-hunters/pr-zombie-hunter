package com.zombie.notifier.mail

object ZombieMailTemplate {

    fun subject(grade: String, prTitle: String): String = when (grade) {
        "SEEDLING" -> "[🌱 새싹 좀비] $prTitle — 슬슬 신경 써주세요"
        "ZOMBIE"   -> "[🧟 좀비 PR] $prTitle — 방치된 지 7일이 넘었습니다"
        "BOSS"     -> "[💀 보스 좀비 발견!] $prTitle — 즉시 처치가 필요합니다"
        else       -> "[PR 알림] $prTitle"
    }

    fun body(grade: String, prTitle: String, prId: String, staleDays: Long, prUrl: String): String = when (grade) {
        "SEEDLING" -> """
            |[새싹 좀비 알림]
            |
            |PR이 ${staleDays}일 동안 방치되고 있습니다. 아직 초기 단계이지만 리뷰를 서둘러 주세요!
            |
            |• PR: $prTitle
            |• ID: $prId
            |• 방치 기간: ${staleDays}일
            |• 링크: $prUrl
            |
            |방치 기간이 7일을 넘으면 좀비 등급으로 승격됩니다.
        """.trimMargin()

        "ZOMBIE" -> """
            |[좀비 PR 경고]
            |
            |PR이 ${staleDays}일 동안 방치되어 좀비 상태가 되었습니다. 빠른 리뷰가 필요합니다!
            |
            |• PR: $prTitle
            |• ID: $prId
            |• 방치 기간: ${staleDays}일
            |• 링크: $prUrl
            |
            |방치 기간이 14일을 넘으면 보스 좀비로 승격되며 전체 팀에 알림이 발송됩니다.
        """.trimMargin()

        "BOSS" -> """
            |[🚨 보스 좀비 긴급 알림]
            |
            |PR이 ${staleDays}일 동안 방치되어 보스 좀비가 되었습니다! 즉시 처치가 필요합니다!
            |
            |• PR: $prTitle
            |• ID: $prId
            |• 방치 기간: ${staleDays}일
            |• 링크: $prUrl
            |
            |더 이상 방치하면 팀 전체의 개발 속도에 영향을 미칩니다. 지금 바로 리뷰해주세요!
        """.trimMargin()

        else -> "PR $prTitle 알림"
    }
}
