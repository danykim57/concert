package com.reservation.ticket.concert.interfaces.schedule

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.domain.QueueStatus
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class QueueRedisScheduler(
    private val queueService: QueueService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(QueueRedisScheduler::class.java)

    // 1분마다 실행
    @Scheduled(fixedRate = 60000)
    fun activateNextBatch() {
        val concertId = "exampleConcert"  // 예제 콘서트 ID
        val queueKey = "concertQueue:$concertId"

        // 상위 50명의 사용자 가져오기
        val usersToActivate = redisTemplate.opsForSet().members(queueKey) ?: emptySet()

        if (usersToActivate.isNotEmpty()) {
            // 상위 50명의 사용자들을 활성화 상태로 변경
            usersToActivate.take(50).forEach { userId ->
                val userToken = UUID.fromString(userId)
                val statusKey = "userStatus:$userToken"

                // 사용자 상태를 WAITING -> ACTIVE로 변경
                redisTemplate.opsForHash<String, String>().put(statusKey, "status", QueueStatus.PASS.name)
                logger.info("Activated user: $userToken")

                // 대기열에서 활성화된 사용자 제거는 다른 스케쥴러에서 처리
            }
        } else {
            logger.info("No users in queue to activate.")
        }
    }
}