package com.reservation.ticket.concert.interfaces.schedule

import com.reservation.ticket.concert.application.service.QueueService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class QueueRedisScheduler(
    private val queueService: QueueService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(QueueScheduler::class.java)

    // 10초마다 실행
    @Scheduled(fixedRate = 10000)
    fun activateNextBatch() {
        val concertId = "exampleConcert"  // 예제 콘서트 ID
        val queueKey = "concertQueue:$concertId"

        // 상위 50명의 사용자 가져오기
        val usersToActivate = redisTemplate.opsForZSet().range(queueKey, 0, 49)

        if (usersToActivate != null && usersToActivate.isNotEmpty()) {
            usersToActivate.forEach { userId ->
                logger.info("Activating user: $userId")
                // 여기서 각 사용자에 대한 활성화 로직을 수행합니다.
            }

            // 활성화된 사용자들을 대기열에서 제거
            redisTemplate.opsForZSet().removeRange(queueKey, 0, 49)
        } else {
            logger.info("No users in queue to activate.")
        }
    }
}