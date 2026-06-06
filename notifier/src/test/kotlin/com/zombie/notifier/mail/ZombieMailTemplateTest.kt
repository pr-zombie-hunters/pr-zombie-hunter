package com.zombie.notifier.mail

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

/**
 * 테스트 대상: ZombieMailTemplate
 * 테스트 크기: Small (단위 테스트)
 *
 * [Small 테스트란?]
 * - 외부 의존성 전혀 없음 (순수 함수 테스트)
 * - Mock 객체도 필요 없음
 * - ZombieMailTemplate의 문자열 생성 로직만 검증
 * - 가장 빠르고 가장 단순한 테스트
 *
 * [이 테스트가 검증하는 것]
 * 1. 등급별 이메일 제목이 올바른 이모지와 문구로 시작하는지
 * 2. 이메일 본문에 PR 정보(제목, ID, 방치일수, 링크)가 포함되는지
 */
class ZombieMailTemplateTest : DescribeSpec({

    describe("ZombieMailTemplate") {

        /**
         * [테스트 그룹 1] 이메일 제목 생성
         * - 등급마다 다른 이모지와 문구가 붙어야 함
         * - 제목에 PR 제목이 포함되어야 함
         */
        context("이메일 제목 생성") {

            it("SEEDLING 등급 제목은 새싹 이모지로 시작한다") {
                // Given: SEEDLING 등급과 PR 제목이 주어졌을 때
                val grade = "SEEDLING"
                val prTitle = "fix/button-color"

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(grade, prTitle)

                // Then: 새싹 이모지로 시작하고 PR 제목이 포함된다
                result shouldStartWith "[🌱 새싹 좀비]"
                result shouldContain prTitle
            }

            it("ZOMBIE 등급 제목은 좀비 이모지로 시작한다") {
                // Given: ZOMBIE 등급과 PR 제목이 주어졌을 때
                val grade = "ZOMBIE"
                val prTitle = "feat/login"

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(grade, prTitle)

                // Then: 좀비 이모지로 시작하고 PR 제목이 포함된다
                result shouldStartWith "[🧟 좀비 PR]"
                result shouldContain prTitle
            }

            it("BOSS 등급 제목은 해골 이모지로 시작한다") {
                // Given: BOSS 등급과 PR 제목이 주어졌을 때
                val grade = "BOSS"
                val prTitle = "feat/auth-refactor"

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(grade, prTitle)

                // Then: 해골 이모지로 시작하고 PR 제목이 포함된다
                result shouldStartWith "[💀 보스 좀비 발견!]"
                result shouldContain prTitle
            }
        }

        /**
         * [테스트 그룹 2] 이메일 본문 생성
         * - 본문에 PR의 핵심 정보가 모두 포함되어야 함
         * - 팀원이 본문만 봐도 어떤 PR인지 알 수 있어야 함
         */
        context("이메일 본문 생성") {

            it("BOSS 등급 본문에는 PR 제목, ID, 방치일수, 링크가 모두 포함된다") {
                // Given: BOSS 등급 PR의 정보가 주어졌을 때
                val grade = "BOSS"
                val prTitle = "feat/auth-refactor"
                val prId = "pr-zombie-hunters/repo#42"
                val staleDays = 15L
                val prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42"

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(grade, prTitle, prId, staleDays, prUrl)

                // Then: PR 제목, ID, 방치일수, 링크가 모두 본문에 포함된다
                result shouldContain prTitle
                result shouldContain prId
                result shouldContain "15"
                result shouldContain prUrl
            }

            it("ZOMBIE 등급 본문에는 방치일수가 정확히 포함된다") {
                // Given: ZOMBIE 등급 PR이 9일 방치됐을 때
                val grade = "ZOMBIE"
                val staleDays = 9L

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(
                    grade, "fix/ui-glitch", "repo#10", staleDays, "https://github.com"
                )

                // Then: 방치일수 9가 본문에 포함된다
                result shouldContain "9"
            }

            it("SEEDLING 등급 본문에는 7일 초과 시 좀비 승격 경고 문구가 포함된다") {
                // Given: SEEDLING 등급 PR이 주어졌을 때
                val grade = "SEEDLING"

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(
                    grade, "chore/cleanup", "repo#5", 4L, "https://github.com"
                )

                // Then: 7일 초과 시 좀비 승격 경고 문구가 포함된다
                //       (팀원에게 위기감을 주는 중요한 문구)
                result shouldContain "7일"
            }
        }
    }
})
