package com.zombie.graphql.mutation

import com.expediagroup.graphql.server.operations.Mutation
import com.zombie.graphql.domain.HunterActionType
import org.springframework.stereotype.Component

@Component
class HunterMutation : Mutation {

    // 처치완료 - 해당 PR을 처치했음을 기록
    // TODO: BA/BB 싱크 후 hunter_action 테이블 필드명 확인 및 DB 저장 로직 연결
    fun markAsHunted(prId: Long, hunterName: String): HunterActionType {
        TODO("hunter_action DB 저장 로직 구현 필요")
    }
}
