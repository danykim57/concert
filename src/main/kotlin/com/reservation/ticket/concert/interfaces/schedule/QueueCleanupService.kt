package com.reservation.ticket.concert.interfaces.schedule

import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class QueueCleanupService(
    private val queueRepository: QueueRepository,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(QueueCleanupService::class.java)
    }


//    @Scheduled(fixedRate = 60000)  // 1분마다 실행
    @Transactional
    fun cleanUpOldQueues() {

        val now = LocalDateTime.now()

        val expiredQueues = queueRepository.findAll().filter { queue ->
            queue.status == QueueStatus.PASS && queue.updatedAt?.plusMinutes(10)?.isBefore(now) == true
        }

        if (expiredQueues.isNotEmpty()) {
            queueRepository.deleteAll(expiredQueues)
            logger.info("Deleted ${expiredQueues.size} expired queues")
        }
    }
}