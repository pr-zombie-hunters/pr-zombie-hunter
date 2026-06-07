package com.zombie.collector

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class CollectorApplicationTests {

	@Mock
	lateinit var pullRequestRepository: PullRequestRepository

	@InjectMocks
	lateinit var collectorService: CollectorService

	// 기존 테스트 유지
	@Test
	fun contextLoads() {
	}

	// SCRUM-83: 중복 PR 스킵 테스트
	@Test
	fun `동일한 PR이 두 번 들어오면 DB에 한 번만 저장된다`() {
		// Given: pr_number=42가 DB에 이미 존재하는 상태
		val existingPr = PullRequest(
			prNumber = 42,
			title = "feat: 테스트용 PR",
			author = "HYcho13",
			repoFullName = "pr-zombie-hunters/pr-zombie-hunter",
			htmlUrl = "https://github.com/pr-zombie-hunters/pr-zombie-hunter/pull/42",
			state = "OPEN",
			lastActivityAt = java.time.LocalDateTime.now()
		)
		`when`(pullRequestRepository.findByPrNumberAndRepoFullName(42, "pr-zombie-hunters/pr-zombie-hunter"))
			.thenReturn(existingPr)

		// When: 같은 pr_number=42로 Webhook 이벤트 재수신
		val duplicatePayload = WebhookPayload(
			action = "opened",
			pullRequest = PullRequestPayload(
				number = 42,
				title = "feat: 테스트용 PR",
				htmlUrl = "https://github.com/pr-zombie-hunters/pr-zombie-hunter/pull/42",
				updatedAt = "2026-06-03T00:00:00Z",
				user = UserPayload(login = "HYcho13"),
				state = "open"
			),
			repository = RepositoryPayload(fullName = "pr-zombie-hunters/pr-zombie-hunter")
		)
		collectorService.handleWebhookEvent(duplicatePayload)

		// Then: DB 저장 메서드가 호출되지 않음 (스킵 확인)
		verify(pullRequestRepository, never()).save(org.mockito.kotlin.any())
	}
}