package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.infrastructure.external.EventApiClient
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ExternalEventListener(
    private val eventApiClient: EventApiClient
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ReservationEvent) {
        eventApiClient.sendReservationEvent()
    }
}