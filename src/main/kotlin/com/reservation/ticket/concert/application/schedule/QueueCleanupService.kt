package com.reservation.ticket.concert.application.schedule

import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QueueCleanupService(
    private val queueRepository: QueueRepository
) {

    @Scheduled(fixedRate = 60000)  // 1분마다 실행
    @Transactional
    fun cleanUpOldQueues() {
        // 현재 시간
        val now = LocalDateTime.now()

        // `updatedAt`에 10분을 더한 값이 현재 시간보다 이전이고, `status`가 "pass"인 모든 큐를 조회
        val expiredQueues = queueRepository.findAll().filter { queue ->
            queue.status == QueueStatus.PASS && queue.updatedAt?.plusMinutes(10)?.isBefore(now) == true
        }

        // 해당하는 큐들을 삭제
        if (expiredQueues.isNotEmpty()) {
            queueRepository.deleteAll(expiredQueues)
            println("Deleted ${expiredQueues.size} expired queues")
        }
    }
}