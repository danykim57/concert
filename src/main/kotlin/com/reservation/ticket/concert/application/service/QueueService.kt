package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.exception.ForbiddenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class QueueService(
    private val queueRepository: QueueRepository
) {
    fun get(userId: UUID): Queue {
        return queueRepository.findByUserId(userId) ?: throw IllegalArgumentException("존재하지 않는 대기열 토큰 입니다.")
    }

    // 유저의 대기 순번을 조회하는 메서드
    fun getUserQueuePosition(userId: UUID): Int {
        // 유저가 대기열에 있는지 확인
        val userQueue = get(userId)

        // 모든 대기열을 시간순으로 조회
        val allQueues = queueRepository.findAllByOrderByCreatedAtAsc()

        // 유저의 대기 순번을 계산 (1부터 시작)
        return allQueues.indexOf(userQueue) + 1
    }

    @Transactional
    fun createQueue(user: User, concert: Concert): String {
        val queue = queueRepository.findByUserIdAndConcert(user.id, concert)
        if (queue == null) {
            return createNewQueue(user, concert).token.toString()
        } else if (queue.status == QueueStatus.WAITING) {
            val leftQueues = queueRepository.findAllByConcertAndStatus(concert, QueueStatus.WAITING)
            return "대기 순번은 $leftQueues 입니다."
        }
        return updateQueue(user, concert).token.toString()
    }

    fun createNewQueue(user: User, concert: Concert): Queue {
        val newQueue = Queue(
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID()
        )
        val result = queueRepository.save(newQueue)
        return result
    }

    fun updateQueue(user: User, concert: Concert): Queue {
        var originalQueue = queueRepository.findByUserIdAndConcert(user.id, concert)
            ?: throw ForbiddenException("존재하지 않은 큐 입니다.")

        originalQueue.status = QueueStatus.PASS

        return queueRepository.save(originalQueue)
    }

    fun delete(queue: Queue) {
        return queueRepository.delete(queue)
    }
}