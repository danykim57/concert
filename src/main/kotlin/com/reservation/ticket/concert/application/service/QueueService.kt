package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.QueueRedisRepository
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.exception.ForbiddenException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class QueueService(
    private val queueRepository: QueueRepository,
    private val queueRedisRepository: QueueRedisRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {

    private val cycleSize = 50  // 사이클당 대기 인원수
    private val cycleWaitTimeMinutes = 5

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

    // 대기열에 사용자 추가 (concertId에 따른 순서)
    fun addToQueue(concertId: String, userId: String): Long {

        val operationNum = redisTemplate.opsForSet().add(concertId, userId)
        val isAdded = if (operationNum != null) true else false

        //Set의 사이즈를 대기 순서로 반환
        return if (isAdded) redisTemplate.opsForSet().size(concertId) ?: 1 else -1
    }

    fun getUserPosition(concertId: String, userId: String): Long? {
        val queueKey = "concertQueue:$concertId"
        // Redis ZSet에서 특정 사용자(userId)의 rank를 조회
        val rank = redisTemplate.opsForZSet().rank(queueKey, userId)
        return rank?.plus(1)  // 0부터 시작하므로 1을 더하여 1-based 순서를 반환
    }

    fun calculateEntryTime(concertId: String, userOrder: Long): Long {
        // 입장 시간 계산: (순서 / 사이클당 인원수) * 사이클당 대기시간
        return (userOrder / cycleSize) * cycleWaitTimeMinutes.toLong()
    }
}