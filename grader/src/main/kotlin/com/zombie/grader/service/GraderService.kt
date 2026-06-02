package com.zombie.grader.service

import com.zombie.grader.domain.ZombieGrade

// 💡 PR의 현재 상태를 받아오기 위한 임시 데이터 클래스입니다. 
// (나중에 수연님이 만드신 Entity 구조와 연결될 예정입니다.)
data class PullRequestInfo(
    val isMerged: Boolean,       // PR이 이미 머지(병합)되었는지 여부
    val staleDays: Long,         // PR이 방치된 일수
    val currentGrade: ZombieGrade // PR의 현재 좀비 등급
)

// 💡 판정된 '새로운 등급'과 '알림 전송 여부'를 한 번에 반환하기 위한 클래스입니다.
data class GraderResult(
    val updatedGrade: ZombieGrade,
    val isNotified: Boolean = false // 기본값은 알림 안 감(false)
)

class GraderService {

    // 🎯 핵심 비즈니스 로직: PR 정보를 바탕으로 새로운 등급을 판정합니다.
    fun evaluateGrade(pr: PullRequestInfo): GraderResult {
        
        // [SCRUM-104] 6. 이미 머지된 PR은 판정할 필요가 없으므로 기존 등급 그대로 반환 (빠른 종료)
        if (pr.isMerged) {
            return GraderResult(updatedGrade = pr.currentGrade, isNotified = false)
        }

        // [SCRUM-103] 5. 이미 최고 등급(보스좀비)인 경우, 더 이상 승격이나 알림이 발생하지 않도록 방지
        if (pr.currentGrade == ZombieGrade.BOSS) {
            return GraderResult(updatedGrade = pr.currentGrade, isNotified = false)
        }

        // [SCRUM-72, 101, 102, 73] 1~4. 방치 일수에 따라 새로운 등급을 계산합니다.
        // 위에서부터 순서대로 일수를 확인하며 내려옵니다.
        val newGrade = when {
            pr.staleDays >= 14 -> ZombieGrade.BOSS     // 14일 이상: 보스좀비
            pr.staleDays >= 7 -> ZombieGrade.ZOMBIE    // 7일 이상 14일 미만: 일반 좀비
            pr.staleDays >= 3 -> ZombieGrade.SEEDLING  // 3일 이상 7일 미만: 새싹 좀비 (수연님 Enum 반영)
            else -> ZombieGrade.NONE                   // 3일 미만: 아무 등급 없음
        }

        // 14일이 지나서 새롭게 '보스 좀비'가 된 경우에만 전체 알림(true) 플래그를 켭니다.
        val shouldNotify = (newGrade == ZombieGrade.BOSS)

        // 최종 판정된 등급과 알림 여부를 묶어서 반환합니다.
        return GraderResult(updatedGrade = newGrade, isNotified = shouldNotify)
    }
}