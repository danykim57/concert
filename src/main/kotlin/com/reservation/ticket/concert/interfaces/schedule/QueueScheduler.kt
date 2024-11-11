package com.reservation.ticket.concert.interfaces.schedule

import com.reservation.ticket.concert.application.service.QueueService
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class QueueScheduler(private val queueService: QueueService) {

    @Scheduled(fixedRate = 5000) //5초마다 run
    fun activateNextUsersInQueue() {
        queueService.activateUsers(limit = 5)
    }
}