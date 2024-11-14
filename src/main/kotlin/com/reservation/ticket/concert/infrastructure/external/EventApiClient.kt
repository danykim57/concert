package com.reservation.ticket.concert.infrastructure.external

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventApiClient {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun sendReservationEvent() {
        log.info("예약 이벤트 전송.")
    }
}