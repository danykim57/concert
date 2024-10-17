package com.reservation.ticket.concert.application.token

import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class QueueStatusChecker(
    private val queueRepository: QueueRepository
) {

    // UserId로 Queue 테이블에서 상태가 PASS인지 확인하는 메서드
    fun isQueueStatusPass(userId: UUID): Boolean {
        val queue = queueRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("해당 유저의 대기열이 존재하지 않습니다.")

        return queue.status == QueueStatus.PASS
    }
}