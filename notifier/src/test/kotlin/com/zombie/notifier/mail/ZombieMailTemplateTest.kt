package com.zombie.notifier.mail

import com.zombie.notifier.messaging.MonsterEvent
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith

/**
 * 테스트 대상: ZombieMailTemplate
 * 테스트 크기: Small (단위 테스트)
 */
class ZombieMailTemplateTest : DescribeSpec({

    // 공통 샘플 이벤트
    val baseEvent = MonsterEvent(
        eventType = "hp_updated",
        prId = "pr-zombie-hunters/repo#42",
        prTitle = "feat/auth-refactor",
        prUrl = "https://github.com/pr-zombie-hunters/repo/pull/42",
        currentHp = 20000,
        maxHp = 40000,
        requiredComments = 4,
    )

    describe("ZombieMailTemplate") {

        /**
         * [테스트 그룹 1] 이메일 제목 생성
         * - 이벤트 타입마다 다른 이모지와 문구가 붙어야 함
         * - 제목에 PR 제목과 HP 정보가 포함되어야 함
         */
        context("이메일 제목 생성") {

            it("hp_updated 이벤트 제목은 좀비 이모지로 시작하고 HP 정보를 포함한다") {
                // Given: HP가 성장한 hp_updated 이벤트가 주어졌을 때
                val event = baseEvent.copy(eventType = "hp_updated")

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(event)

                // Then: 좀비 이모지로 시작하고 HP와 PR 제목이 포함된다
                result shouldStartWith "[🧟 좀비 PR]"
                result shouldContain event.prTitle
                result shouldContain "20000"
            }

            it("defeated 이벤트 제목은 축하 이모지로 시작한다") {
                // Given: 처치 완료된 defeated 이벤트가 주어졌을 때
                val event = baseEvent.copy(eventType = "defeated")

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(event)

                // Then: 축하 이모지로 시작하고 PR 제목이 포함된다
                result shouldStartWith "[🎉 처치 완료]"
                result shouldContain event.prTitle
            }

            it("revived 이벤트 제목은 해골 이모지로 시작한다") {
                // Given: Revert로 부활한 revived 이벤트가 주어졌을 때
                val event = baseEvent.copy(eventType = "revived")

                // When: subject()를 호출하면
                val result = ZombieMailTemplate.subject(event)

                // Then: 해골 이모지로 시작하고 PR 제목이 포함된다
                result shouldStartWith "[💀 몬스터 부활!]"
                result shouldContain event.prTitle
            }
        }

        /**
         * [테스트 그룹 2] 이메일 본문 생성
         * - 본문에 HP, 필요 코멘트 수, PR 링크 등 핵심 정보가 모두 포함되어야 함
         */
        context("이메일 본문 생성") {

            it("hp_updated 본문에는 현재 HP와 처치까지 필요한 코멘트 수가 포함된다") {
                // Given: HP가 20000이고 처치에 4개 코멘트가 필요한 이벤트가 주어졌을 때
                val event = baseEvent.copy(
                    eventType = "hp_updated",
                    currentHp = 20000,
                    requiredComments = 4,
                )

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(event)

                // Then: 현재 HP, 필요 코멘트 수, 링크가 본문에 포함된다
                result shouldContain "20000"
                result shouldContain "4"
                result shouldContain event.prUrl
            }

            it("hp_updated 본문에는 6시간마다 HP 2배 경고 문구가 포함된다") {
                // Given: hp_updated 이벤트가 주어졌을 때
                val event = baseEvent.copy(eventType = "hp_updated")

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(event)

                // Then: 팀원에게 위기감을 주는 HP 성장 경고 문구가 포함된다
                result shouldContain "6시간"
            }

            it("revived 본문에는 부활 HP와 처치까지 필요한 코멘트 수가 포함된다") {
                // Given: HP 10000으로 부활한 revived 이벤트가 주어졌을 때
                val event = baseEvent.copy(
                    eventType = "revived",
                    currentHp = 10000,
                    requiredComments = 2,
                )

                // When: body()를 호출하면
                val result = ZombieMailTemplate.body(event)

                // Then: 부활 HP와 필요 코멘트 수가 본문에 포함된다
                result shouldContain "10000"
                result shouldContain "2"
            }
        }
    }
})
