package com.reservation.ticket.concert.interfaces.schedule

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RemoveFromQueueScheduler(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(RemoveFromQueueScheduler::class.java)

    // 1분마다 실행, 대기열에서 상위 50명 제거
//    @Scheduled(fixedRate = 60000)
    fun removeFromQueue() {
        val concertId = "exampleConcert"  // 예제 콘서트 ID
        val queueKey = "concertQueue:$concertId"

        // 대기열에서 상위 50명 제거
        val usersToRemove = redisTemplate.opsForSet().members(queueKey) ?: emptySet()

        if (usersToRemove.isNotEmpty()) {
            // 상위 50명의 사용자 제거
            usersToRemove.take(50).forEach { userId ->
                redisTemplate.opsForSet().remove(queueKey, userId)
                logger.info("Removed user from queue: $userId")
            }
        } else {
            logger.info("No users to remove from queue.")
        }
    }
}
