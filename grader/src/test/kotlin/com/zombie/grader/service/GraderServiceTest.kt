package com.zombie.grader.service

import com.zombie.grader.domain.ZombieGrade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GraderServiceTest {

    // 테스트를 수행할 Grader 서비스 객체를 준비합니다.
    private val graderService = GraderService()

    @Test
    @DisplayName("[SCRUM-72] PR 방치 3일 미만일 때 -> 등급 없음")
    fun `evaluateGrade returns NONE when staleDays is less than 3`() {
        // Given: 방치된 지 2일 된 PR이 주어졌을 때
        val pr = PullRequestInfo(isMerged = false, staleDays = 2, currentGrade = ZombieGrade.NONE)
        
        // When: 등급 판정을 실행하면
        val result = graderService.evaluateGrade(pr)
        
        // Then: 등급은 NONE이고, 알림은 가지 않아야 합니다.
        assertEquals(ZombieGrade.NONE, result.updatedGrade)
        assertEquals(false, result.isNotified)
    }

    @Test
    @DisplayName("[SCRUM-101] PR 방치 정확히 3일일 때 -> 새싹좀비(SEEDLING)")
    fun `evaluateGrade returns SEEDLING when staleDays is 3`() {
        // Given: 방치 일수가 정확히 3일인 PR
        val pr = PullRequestInfo(isMerged = false, staleDays = 3, currentGrade = ZombieGrade.NONE)
        
        // When: 등급 판정 실행
        val result = graderService.evaluateGrade(pr)
        
        // Then: 새싹좀비(SEEDLING) 등급을 받아야 합니다.
        assertEquals(ZombieGrade.SEEDLING, result.updatedGrade)
    }

    @Test
    @DisplayName("[SCRUM-102] PR 방치 정확히 7일일 때 -> 좀비(ZOMBIE) 승격")
    fun `evaluateGrade returns ZOMBIE when staleDays is 7`() {
        // Given: 방치 일수가 7일인 새싹좀비 PR
        val pr = PullRequestInfo(isMerged = false, staleDays = 7, currentGrade = ZombieGrade.SEEDLING)
        
        // When: 등급 판정 실행
        val result = graderService.evaluateGrade(pr)
        
        // Then: 일반 좀비(ZOMBIE)로 승격되어야 합니다.
        assertEquals(ZombieGrade.ZOMBIE, result.updatedGrade)
    }

    @Test
    @DisplayName("[SCRUM-73] PR 방치 14일 이상일 때 -> 보스좀비(BOSS) 승격 및 알림")
    fun `evaluateGrade returns BOSS and notification flag when staleDays is 14`() {
        // Given: 방치 일수가 14일인 일반 좀비 PR
        val pr = PullRequestInfo(isMerged = false, staleDays = 14, currentGrade = ZombieGrade.ZOMBIE)
        
        // When: 등급 판정 실행
        val result = graderService.evaluateGrade(pr)
        
        // Then: 보스좀비(BOSS)로 승격되고, 전체 알림(true)이 발생해야 합니다.
        assertEquals(ZombieGrade.BOSS, result.updatedGrade)
        assertEquals(true, result.isNotified)
    }

    @Test
    @DisplayName("[SCRUM-103] 이미 보스좀비(BOSS)인 경우 -> 등급 변화 없음 (알림 미발송)")
    fun `evaluateGrade ignores PR if already BOSS`() {
        // Given: 이미 최고 등급(보스좀비)을 받은 PR이 15일째 방치 중일 때
        val pr = PullRequestInfo(isMerged = false, staleDays = 15, currentGrade = ZombieGrade.BOSS)
        
        // When: 스케줄러가 등급 판정을 한 번 더 실행하면
        val result = graderService.evaluateGrade(pr)
        
        // Then: 중복 승격 방지를 위해 등급은 그대로 유지되고, 알림도 다시 가지 않습니다.
        assertEquals(ZombieGrade.BOSS, result.updatedGrade)
        assertEquals(false, result.isNotified)
    }

    @Test
    @DisplayName("[SCRUM-104] PR이 머지된 상태일 때 -> 판정 대상에서 제외")
    fun `evaluateGrade ignores PR if already merged`() {
        // Given: 10일 방치되었지만 이미 머지(isMerged = true)가 완료된 PR
        val pr = PullRequestInfo(isMerged = true, staleDays = 10, currentGrade = ZombieGrade.SEEDLING)
        
        // When: 스케줄러가 등급 판정을 시도하면
        val result = graderService.evaluateGrade(pr)
        
        // Then: 판정 대상에서 완전히 제외되어 기존 등급이 그대로 유지됩니다.
        assertEquals(ZombieGrade.SEEDLING, result.updatedGrade) 
    }
}