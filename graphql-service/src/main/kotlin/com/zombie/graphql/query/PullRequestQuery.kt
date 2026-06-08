package com.zombie.graphql.query

import com.expediagroup.graphql.server.operations.Query
import com.zombie.graphql.domain.PullRequestType
import com.zombie.graphql.domain.ZombieGrade
import org.springframework.stereotype.Component

@Component
class PullRequestQuery : Query {

    // 목록 조회 - grade 필터 없으면 전체 반환
    // TODO: BA/BB 싱크 후 실제 DB 조회 로직 연결
    fun pullRequests(grade: ZombieGrade? = null): List<PullRequestType> {
        return emptyList()
    }

    // 단건 조회 - id로 조회
    // TODO: BA/BB 싱크 후 실제 DB 조회 로직 연결
    fun pullRequest(id: Long): PullRequestType? {
        return null
    }
}
