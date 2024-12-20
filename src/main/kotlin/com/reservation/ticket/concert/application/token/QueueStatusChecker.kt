package com.reservation.ticket.concert.application.token

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.exception.ForbiddenException
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class QueueStatusChecker(
    private val queueRepository: QueueRepository,
    private val queueService: QueueService,
) {
    fun checkTokenValid(token: String, userId: UUID) {
        if(token.isEmpty()) throw ForbiddenException("유효하지 않은 대기열 토큰입니다.")
        isQueueStatusPass(userId)
    }

    // UserId로 Queue 테이블에서 상태가 PASS인지 확인하는 메서드
    fun isQueueStatusPass(userId: UUID): Boolean {
        val queue = queueRepository.findByUserId(userId)
            ?: throw UnprocessableEntityException("아직 대기중입니다.")
        return queue.status == QueueStatus.PASS
    }

    // 사용자 대기열 유효성 검사
    fun validateUserInQueue(concertId: String, userId: String): Boolean {
        val position = queueService.getUserPosition(concertId, userId)
        if (position != null) {
            val entryTime = queueService.calculateEntryTime(concertId, position)
            return false
        }
        return true
    }
}