package com.reservation.ticket.concert.interfaces.schedule

import com.reservation.ticket.concert.application.service.OutboxService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutBoxScheduler(
    private val outboxService: OutboxService
) {

    @Scheduled(fixedDelayString = "10000")
    fun process() {
        outboxService.publishUnprocessedEvents()
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    fun retryFailedEvents() {
        outboxService.publishUnprocessedEvents()
    }

}