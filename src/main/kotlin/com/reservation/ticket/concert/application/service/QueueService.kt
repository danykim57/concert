package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.QueueRedisRepository
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.exception.ForbiddenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class QueueService(
    private val queueRepository: QueueRepository,
    private val queueRedisRepository: QueueRedisRepository,
) {
    fun get(userId: UUID): Queue {
        return queueRepository.findByUserId(userId) ?: throw IllegalArgumentException("존재하지 않는 대기열 토큰 입니다.")
    }

    fun getUserQueuePosition(userId: UUID): Int {

        val userQueue = get(userId)

        val allQueues = queueRepository.findAllByOrderByCreatedAtAsc()

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
        val originalQueue = queueRepository.findByUserIdAndConcert(user.id, concert)
            ?: throw ForbiddenException("존재하지 않은 큐 입니다.")

        originalQueue.status = QueueStatus.PASS

        return queueRepository.save(originalQueue)
    }

    fun delete(queue: Queue) {
        return queueRepository.delete(queue)
    }

    fun addUserToWaitQueue(userId: String, timestamp: Double) {
        queueRedisRepository.addToWaitQueue(userId, timestamp)
    }

    fun getUserRankInQueue(userId: String): Long? {
        return queueRedisRepository.getUserRank(userId)
    }

    // 활성 상태로 전환
    fun activateUsers(limit: Long) {
        val usersToActivate = queueRedisRepository.getUsersInRange(0, limit - 1)
        usersToActivate.forEach { userId ->
            queueRedisRepository.removeFromWaitQueue(userId)
            queueRedisRepository.setUserActive(userId, ttl = 300) // 5분
        }
    }

    // 활성 상태 사용자 가져오기
    fun getActiveUsers(): Set<String> {
        return queueRedisRepository.getActiveUsers()
    }

    // 결제 완료 후 사용자 제거
    fun completeUserProcess(userId: String) {
        queueRedisRepository.completeUser(userId)
    }
}